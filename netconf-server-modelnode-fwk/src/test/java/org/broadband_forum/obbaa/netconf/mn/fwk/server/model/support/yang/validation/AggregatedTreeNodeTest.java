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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.AggregatedTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class AggregatedTreeNodeTest extends AbstractSchemaMountTest{

    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMergeFollowedByMerge() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "              <name3>Example1</name3>\n" + 
                "              <aggregatedExampleList1Container1>\n" + 
                "                <test1>changeddummyTest1</test1>\n" + 
                "              </aggregatedExampleList1Container1>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Roll</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:aggregatedExampleList1Container1>\n" + 
                "                    <smt:test1>changeddummyTest1</smt:test1>\n" + 
                "                  </smt:aggregatedExampleList1Container1>\n" + 
                "                  <smt:name1>Example1key1</smt:name1>\n" + 
                "                  <smt:name2>Example1key2</smt:name2>\n" + 
                "                  <smt:name3>Example1</smt:name3>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMergeFollowedByRemove() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);
        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);       
    }

    @Test
    public void testMergeFollowedByRemoveInnerLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>\n" + 
                "            <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);       
    }

    @Test
    public void testMergeFollowedByCreate() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <merge>X<create> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleContainerList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }


    @Test
    public void testMergeFollowedByDelete() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <merge>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleContainerList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }


    @Test
    public void testMergeFollowedByReplace() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <merge>X<replace> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleContainerList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }


    @Test
    public void testDeleteFollowedByCreate() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<create> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }


    @Test
    public void testDeleteFollowedByMerge() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }


    @Test
    public void testDeleteFollowedByDelete() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testDeleteFollowedByRemove() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<remove> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testDeleteFollowedByReplace() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<replace> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testDeleteFollowedByAnyOperationInnerLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <delete>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByCreate() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<create> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByMerge() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByDelete() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByRemove() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<remove> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByReplace() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<replace> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testCreateFollowedByAnyOperationInnerLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <create>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testRemoveFollowedByCreate() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <remove>X<create> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testRemoveFollowedByMerge() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <remove>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testRemoveFollowedByDelete() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <remove>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testRemoveFollowedByReplace() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "              <name3>Example1</name3>\n" + 
                "              <aggregatedExampleList1Container1>\n" + 
                "                <test1>changeddummyTest1</test1>\n" + 
                "              </aggregatedExampleList1Container1>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Roll</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:aggregatedExampleList1Container1>\n" + 
                "                    <smt:test1>changeddummyTest1</smt:test1>\n" + 
                "                  </smt:aggregatedExampleList1Container1>\n" + 
                "                  <smt:name1>Example1key1</smt:name1>\n" + 
                "                  <smt:name2>Example1key2</smt:name2>\n" + 
                "                  <smt:name3>Example1</smt:name3>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testRemoveFollowedByRemove() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);
        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testRemoveFollowedByReplaceNestedLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "              <name3>replacedExample1</name3>\n" + 
                "              <aggregatedExampleList1Container1>\n" + 
                "                <test1>replaceddummyTest1</test1>\n" + 
                "              </aggregatedExampleList1Container1>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Roll</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:aggregatedExampleList1Container1>\n" + 
                "                    <smt:test1>replaceddummyTest1</smt:test1>\n" + 
                "                  </smt:aggregatedExampleList1Container1>\n" + 
                "                  <smt:name1>Example1key1</smt:name1>\n" + 
                "                  <smt:name2>Example1key2</smt:name2>\n" + 
                "                  <smt:name3>replacedExample1</smt:name3>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testRemoveFollowedByRemoveNestedLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Roll</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testReplaceFollowedByCreate() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <replace>X<create> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testReplaceFollowedByDelete() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <replace>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testReplaceFollowedByMerge() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <replace>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testReplaceFollowedByReplace() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example3key1</name1>" +
                "           <name2>Example3key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "              <name3>replacedExample1</name3>\n" + 
                "              <aggregatedExampleList1Container1>\n" + 
                "                <test1>replaceddummyTest1</test1>\n" + 
                "              </aggregatedExampleList1Container1>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example3key1</name1>\n" + 
                "              <name2>Example3key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Roll</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:aggregatedExampleList1Container1>\n" + 
                "                    <smt:test1>replaceddummyTest1</smt:test1>\n" + 
                "                  </smt:aggregatedExampleList1Container1>\n" + 
                "                  <smt:name1>Example1key1</smt:name1>\n" + 
                "                  <smt:name2>Example1key2</smt:name2>\n" + 
                "                  <smt:name3>replacedExample1</smt:name3>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example3key1</smt:name1>\n" + 
                "                  <smt:name2>Example3key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testReplaceFollowedByReplaceInnerLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example3key1</name1>" +
                "           <name2>Example3key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <replace>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testReplaceFollowedByRemove() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example3key1</name1>" +
                "           <name2>Example3key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testReplaceFollowedByRemoveInnerLevel() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>replacedExample1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>replaceddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example3key1</name1>" +
                "           <name2>Example3key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <replace>X<merge> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testMergeFollowedByMergeLeafListScenario1() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "     <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>" +
                "     <aggregatedExampleLeafList1>aggregatedLeafList2</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>modifiedAggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>modifiedAggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>\n" + 
                "          <aggregatedExampleLeafList1>aggregatedLeafList2</aggregatedExampleLeafList1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);
        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleLeaf1>modifiedAggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "              <smt:aggregatedExampleLeafList1>aggregatedLeafList1</smt:aggregatedExampleLeafList1>\n" + 
                "              <smt:aggregatedExampleLeafList1>aggregatedLeafList2</smt:aggregatedExampleLeafList1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMergeFollowedByMergeLeafListScenario2() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>" +
                "     <aggregatedExampleLeafList1>aggregatedLeafList2</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>\n" + 
                "          <aggregatedExampleLeafList1>aggregatedLeafList2</aggregatedExampleLeafList1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleLeaf1>aggregatedLeaf1</smt:aggregatedExampleLeaf1>\n" + 
                "              <smt:aggregatedExampleLeafList1>aggregatedLeafList1</smt:aggregatedExampleLeafList1>\n" + 
                "              <smt:aggregatedExampleLeafList1>aggregatedLeafList2</smt:aggregatedExampleLeafList1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMergeFollowedByMergeLeafListScenario3() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "     <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeafList1>aggregatedLeafList2</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "Cannot merge leaf-lists from more than one edit-config, found multiple leaf-lists of type: (schema-mount-test?revision=2018-01-03)aggregatedExampleLeafList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testMergeFollowedByMergeLeafListScenario4() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "     <aggregatedExampleLeafList1>aggregatedLeafList1</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeafList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">aggregatedLeafList1</aggregatedExampleLeafList1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "Cannot merge leaf-lists from more than one edit-config, found multiple leaf-lists of type: (schema-mount-test?revision=2018-01-03)aggregatedExampleLeafList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario1() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +  
                "       <case1Container1>" +
                "        <case1Container1Leaf1>enabled</case1Container1Leaf1>" +
                "       </case1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +   
                "       <case2Container1>" +
                "        <case2Container1Leaf1>enabled</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <case2Leaf1>rock</case2Leaf1>\n" + 
                "          <case2Container1>" +
                "           <case2Container1Leaf1>enabled</case2Container1Leaf1>" +
                "          </case2Container1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2Container1>\n" + 
                "                <smt:case2Container1Leaf1>enabled</smt:case2Container1Leaf1>\n" + 
                "              </smt:case2Container1>\n" + 
                "              <smt:case2Leaf1>rock</smt:case2Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario2() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +  
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Container1>" +
                "        <case1Container1Leaf1>enabled</case1Container1Leaf1>" +
                "       </case1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <case1Leaf1>rock</case1Leaf1>\n" + 
                "          <case1Container1>" +
                "           <case1Container1Leaf1>enabled</case1Container1Leaf1>" +
                "          </case1Container1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);
        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case1Container1>\n" + 
                "                <smt:case1Container1Leaf1>enabled</smt:case1Container1Leaf1>\n" + 
                "              </smt:case1Container1>\n" + 
                "              <smt:case1Leaf1>rock</smt:case1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario3() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +  
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Container1>" +
                "        <case2Container1Leaf1>enabled</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2Container1>" +
                "         <case2Container1Leaf1>enabled</case2Container1Leaf1>" +
                "        </case2Container1>" +
                "        <case2List1>" +
                "         <name>list1</name>" +
                "         <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "        </case2List1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2Container1>\n" + 
                "                <smt:case2Container1Leaf1>enabled</smt:case2Container1Leaf1>\n" + 
                "              </smt:case2Container1>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list1</smt:name>\n" + 
                "                <smt:nonKeyLeaf>nonkeyleaf1</smt:nonKeyLeaf>\n" + 
                "              </smt:case2List1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario4() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +  
                "       <case2List1>" +
                "        <name>list1</name>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +
                "       <case2Container1>" +
                "        <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "       <case2List1>" +
                "        <name>list2</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2Leaf1>rock</case2Leaf1>" +
                "        <case2Container1>" +
                "         <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "        </case2Container1>" +
                "        <case2List1>" +
                "         <name>list1</name>" +
                "         <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "        </case2List1>" +
                "        <case2List1>" +
                "         <name>list2</name>" +
                "         <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "        </case2List1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2Container1>\n" + 
                "                <smt:case2Container1Leaf1>changed</smt:case2Container1Leaf1>\n" + 
                "              </smt:case2Container1>\n" + 
                "              <smt:case2Leaf1>rock</smt:case2Leaf1>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list1</smt:name>\n" + 
                "                <smt:nonKeyLeaf>nonkeyleaf1</smt:nonKeyLeaf>\n" + 
                "              </smt:case2List1>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list2</smt:name>\n" + 
                "                <smt:nonKeyLeaf>nonkeyleaf1</smt:nonKeyLeaf>\n" + 
                "              </smt:case2List1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario5() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +
                "       <case2Container1>" +
                "        <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "       <case2List1>" +
                "        <name>list2</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case1Leaf1>rock</case1Leaf1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case1Leaf1>rock</smt:case1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigChoiceCaseScenario6() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +
                "       <case2Container1>" +
                "        <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "       <case2List1>" +
                "        <name>list2</name>" +
                "        <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\"/>" +
                "       <case2List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "        <name>list2</name>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <case2Leaf1 ns0:operation=\"remove\"/>" +
                "         <case2List1>" +
                "          <name>list1</name>" +
                "          <nonKeyLeaf>nonkeyleaf1</nonKeyLeaf>" +
                "         </case2List1>" +
                "         <case2Container1>" +
                "          <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "         </case2Container1>" +
                "         <case2List1 ns0:operation=\"remove\">" +
                "          <name>list2</name>" +
                "         </case2List1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);
        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2Container1>\n" + 
                "                <smt:case2Container1Leaf1>changed</smt:case2Container1Leaf1>\n" + 
                "              </smt:case2Container1>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list1</smt:name>\n" + 
                "                <smt:nonKeyLeaf>nonkeyleaf1</smt:nonKeyLeaf>\n" + 
                "              </smt:case2List1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario1() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +
                "       <case1Container1>" +
                "        <case1Container1Leaf1>changed</case1Container1Leaf1>" +
                "       </case1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "            <smt:nestedCase1Leaf1>true</smt:nestedCase1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario2() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case1Leaf1>rock</case1Leaf1>" +
                "       <case1Container1>" +
                "        <case1Container1Leaf1>changed</case1Container1Leaf1>" +
                "       </case1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "            <smt:nestedCase2Leaf1>true</smt:nestedCase2Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario3() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "            <smt:nestedCase2Leaf1>true</smt:nestedCase2Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario4() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2Leaf1>rock</case2Leaf1>" +
                "       <case2Container1>" +
                "        <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "       </case2Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2Leaf1>rock</case2Leaf1>" +
                "         <case2Container1>" +
                "          <case2Container1Leaf1>changed</case2Container1Leaf1>" +
                "         </case2Container1>" +
                "         <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2Container1>\n" + 
                "                <smt:case2Container1Leaf1>changed</smt:case2Container1Leaf1>\n" + 
                "              </smt:case2Container1>\n" + 
                "              <smt:case2Leaf1>rock</smt:case2Leaf1>\n" + 
                "              <smt:nestedCase2Leaf1>true</smt:nestedCase2Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario5() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase2Leaf1>true</nestedCase2Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:nestedCase2Leaf1>true</smt:nestedCase2Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario6() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:dummy=\"urn:dummy\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>modified</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "         <nestedCase1Container1>" +
                "          <nestedCase1Container1Leaf1>modified</nestedCase1Container1Leaf1>" +
                "         </nestedCase1Container1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:nestedCase1Container1>\n" + 
                "                <smt:nestedCase1Container1Leaf1>modified</smt:nestedCase1Container1Leaf1>\n" + 
                "              </smt:nestedCase1Container1>\n" + 
                "              <smt:nestedCase1Leaf1>true</smt:nestedCase1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario7() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "        <nestedCase1Container1Leaf1>modified</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "         <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "         <nestedCase1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "         </nestedCase1Container1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" +  
                "              <smt:nestedCase1Leaf1>true</smt:nestedCase1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario8() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">" +
                "        <nestedCase1Container1Leaf1>modified</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <merge>X<delete> on node : (schema-mount-test?revision=2018-01-03)nestedCase1Container1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario9() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <nestedCase1Container1>" +
                "        <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "       </nestedCase1Container1>" +
                "       <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nestedCase1Leaf1>modified</nestedCase1Leaf1>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2List1>" +
                "         <name>list1</name>" +
                "         <nestedCase1Leaf1>modified</nestedCase1Leaf1>" +
                "        </case2List1>" +
                "        <nestedCase1Container1>" +
                "         <nestedCase1Container1Leaf1>changed</nestedCase1Container1Leaf1>" +
                "        </nestedCase1Container1>" +
                "        <nestedCase1Leaf1>true</nestedCase1Leaf1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list1</smt:name>\n" + 
                "                <smt:nestedCase1Leaf1>modified</smt:nestedCase1Leaf1>\n" + 
                "              </smt:case2List1>\n" + 
                "              <smt:nestedCase1Container1>\n" + 
                "                <smt:nestedCase1Container1Leaf1>changed</smt:nestedCase1Container1Leaf1>\n" + 
                "              </smt:nestedCase1Container1>\n" + 
                "              <smt:nestedCase1Leaf1>true</smt:nestedCase1Leaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario10() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nestedCase1Leaf1>modified</nestedCase1Leaf1>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2List1>" +
                "        <name>list1</name>" +
                "        <nestedCase1Leaf1>changed</nestedCase1Leaf1>" +
                "       </case2List1>" +
                "       <case2List1>" +
                "        <name>list2</name>" +
                "        <nestedCase2Leaf1>changed</nestedCase2Leaf1>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2List1>" +
                "         <name>list1</name>" +
                "         <nestedCase1Leaf1>changed</nestedCase1Leaf1>" +
                "        </case2List1>" +
                "        <case2List1>" +
                "         <name>list2</name>" +
                "         <nestedCase2Leaf1>changed</nestedCase2Leaf1>" +
                "        </case2List1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list1</smt:name>\n" + 
                "                <smt:nestedCase1Leaf1>changed</smt:nestedCase1Leaf1>\n" + 
                "              </smt:case2List1>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list2</smt:name>\n" + 
                "                <smt:nestedCase2Leaf1>changed</smt:nestedCase2Leaf1>\n" + 
                "              </smt:case2List1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testAggregatedEditConfigNestedChoiceCaseScenario11() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "        <name>list1</name>" +
                "        <nestedCase1Leaf1>modified</nestedCase1Leaf1>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "       <case2List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "        <name>list1</name>" +
                "       </case2List1>" +
                "       <case2List1>" +
                "        <name>list2</name>" +
                "        <nestedCase2Leaf1>changed</nestedCase2Leaf1>" +
                "       </case2List1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "        <case2List1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"remove\">" +
                "         <name>list1</name>" +
                "        </case2List1>" +
                "        <case2List1>" +
                "         <name>list2</name>" +
                "         <nestedCase2Leaf1>changed</nestedCase2Leaf1>" +
                "        </case2List1>" +
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:case2List1>\n" + 
                "                <smt:name>list2</smt:name>\n" + 
                "                <smt:nestedCase2Leaf1>changed</smt:nestedCase2Leaf1>\n" + 
                "              </smt:case2List1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMergeFollowedByMergeMultipleRequests() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Jill</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest2</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf2</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";

        String expectedMergedEditRequest = "<validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "  <xml-subtree>\n" + 
                "    <plugType>PLUG-1.0</plugType>\n" + 
                "    <schemaMountPoint>\n" + 
                "      <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "        <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "        <aggregatedExampleTopContainer>\n" + 
                "          <aggregatedExampleLeaf1>changedaggregatedLeaf2</aggregatedExampleLeaf1>\n" + 
                "          <aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleContainerLeaf1>rock'N'Jill</aggregatedExampleContainerLeaf1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example1key1</name1>\n" + 
                "              <name2>Example1key2</name2>\n" + 
                "              <name3>Example1</name3>\n" + 
                "              <aggregatedExampleList1Container1>\n" + 
                "                <test1>changeddummyTest2</test1>\n" + 
                "              </aggregatedExampleList1Container1>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "            <aggregatedExampleContainerList1>\n" + 
                "              <name1>Example2key1</name1>\n" + 
                "              <name2>Example2key2</name2>\n" + 
                "            </aggregatedExampleContainerList1>\n" + 
                "          </aggregatedExampleContainer1>\n" + 
                "        </aggregatedExampleTopContainer>\n" + 
                "      </schemaMount>\n" + 
                "    </schemaMountPoint>\n" + 
                "  </xml-subtree>\n" + 
                "</validation>\n";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, false);

        NetConfResponse netconfResponse = editConfig(m_server, m_clientInfo, expectedMergedEditRequest, true);

        String response = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "  <data>\n" + 
                "    <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "      <validation:xml-subtree>\n" + 
                "        <validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "        <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "          <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "            <smt:aggregatedExampleEnabled>true</smt:aggregatedExampleEnabled>\n" + 
                "            <smt:aggregatedExampleTopContainer>\n" + 
                "              <smt:aggregatedExampleContainer1>\n" + 
                "                <smt:aggregatedExampleContainerLeaf1>rock'N'Jill</smt:aggregatedExampleContainerLeaf1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:aggregatedExampleList1Container1>\n" + 
                "                    <smt:test1>changeddummyTest2</smt:test1>\n" + 
                "                  </smt:aggregatedExampleList1Container1>\n" + 
                "                  <smt:name1>Example1key1</smt:name1>\n" + 
                "                  <smt:name2>Example1key2</smt:name2>\n" + 
                "                  <smt:name3>Example1</smt:name3>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "                <smt:aggregatedExampleContainerList1>\n" + 
                "                  <smt:name1>Example2key1</smt:name1>\n" + 
                "                  <smt:name2>Example2key2</smt:name2>\n" + 
                "                </smt:aggregatedExampleContainerList1>\n" + 
                "              </smt:aggregatedExampleContainer1>\n" + 
                "              <smt:aggregatedExampleLeaf1>changedaggregatedLeaf2</smt:aggregatedExampleLeaf1>\n" + 
                "            </smt:aggregatedExampleTopContainer>\n" + 
                "          </smt:schemaMount>\n" + 
                "        </schemaMountPoint>\n" + 
                "      </validation:xml-subtree>\n" + 
                "    </validation:validation>\n" + 
                "  </data>\n" + 
                "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMergeFollowedByDeleteMultipleRequests() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">"+
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "merging multiple edit payload failed because of edit operation clash : <merge>X<delete> on node : (schema-mount-test?revision=2018-01-03)aggregatedExampleTopContainer";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testConfigRequestsWithMultipleRootNodesScenario2() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " <otherRootContainer xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <otherRootContainerLeaf>aggregatedLeaf1</otherRootContainerLeaf>" +
                " </otherRootContainer>" +
                " <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <key1>list1</key1>" +
                " </otherRootList>" +
                " <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <key1>list2</key1>" +
                " </otherRootList>" +
                " </config>"+
                " </edit-config-payload>" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf1>changedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>" +
                " <otherRootContainer xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <otherRootContainerLeaf>aggregatedLeaf1</otherRootContainerLeaf>" +
                " </otherRootContainer>" +
                " <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <key1>list1</key1>" +
                " </otherRootList>" +
                " <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <key1>list2</key1>" +
                " </otherRootList>" +
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedMergedEditRequest = " <config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">\n" + 
                "  <otherRootContainer xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "    <otherRootContainerLeaf>aggregatedLeaf1</otherRootContainerLeaf>\n" + 
                "  </otherRootContainer>\n" + 
                "  <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "    <key1>list1</key1>\n" + 
                "  </otherRootList>\n" + 
                "  <otherRootList xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "    <key1>list2</key1>\n" + 
                "  </otherRootList>\n" + 
                "  <validation xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "    <xml-subtree>\n" + 
                "      <plugType>PLUG-1.0</plugType>\n" + 
                "      <schemaMountPoint>\n" + 
                "        <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">\n" + 
                "          <aggregatedExampleEnabled>true</aggregatedExampleEnabled>\n" + 
                "          <aggregatedExampleTopContainer>\n" + 
                "            <aggregatedExampleContainer1>\n" + 
                "              <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>\n" + 
                "              <aggregatedExampleContainerList1>\n" + 
                "                <aggregatedExampleList1Container1>\n" + 
                "                  <test1>dummyTest1</test1>\n" + 
                "                </aggregatedExampleList1Container1>\n" + 
                "                <name1>Example1key1</name1>\n" + 
                "                <name2>Example1key2</name2>\n" + 
                "              </aggregatedExampleContainerList1>\n" + 
                "            </aggregatedExampleContainer1>\n" + 
                "            <aggregatedExampleLeaf1>changedLeaf1</aggregatedExampleLeaf1>\n" + 
                "          </aggregatedExampleTopContainer>\n" + 
                "        </schemaMount>\n" + 
                "      </schemaMountPoint>\n" + 
                "    </xml-subtree>\n" + 
                "  </validation>\n" + 
                "</config>";
        verifyMergeRequestSuccess(request1, expectedMergedEditRequest, true);

    }

    @Test
    public void testAggregateRequestWithoutKey() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"+
                "           <name1>Example1key1</name1>" +
                "           <aggregatedExampleList1Container1>" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "Could not find the key element (schema-mount-test?revision=2018-01-03)name2 for the node (schema-mount-test?revision=2018-01-03)aggregatedExampleContainerList1";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testAggregateRequestWithForbiddenRequest() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"replace\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" +
                "           <aggregatedExampleList1Container1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">" +
                "            <test1>dummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>aggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "specified operation merge is forbidden on the node (schema-mount-test?revision=2018-01-03)aggregatedExampleList1Container1 inside the parent node operation replace ";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    @Test
    public void testAggregateRequestWithInvalidNode() throws Exception {
        String request1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<aggregated-edit-config xmlns=\"http://www.test-company.com/solutions/anv-netconf-stack\">" +
                "<edit-config-payload>" +
                " <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "     <aggregatedExampleLeaf5>aggregatedLeaf1</aggregatedExampleLeaf5>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                "  <edit-config-payload>" +
                "  <config>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <aggregatedExampleEnabled>true</aggregatedExampleEnabled>" +
                "     <aggregatedExampleTopContainer>"+
                "      <aggregatedExampleContainer1>" +
                "       <aggregatedExampleContainerLeaf1>rock'N'Roll</aggregatedExampleContainerLeaf1>" +
                "       <aggregatedExampleContainerList1 xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"create\">"+
                "           <name1>Example1key1</name1>" +
                "           <name2>Example1key2</name2>" + 
                "           <name3>Example1</name3>" + 
                "           <aggregatedExampleList1Container1>" +
                "            <test1>changeddummyTest1</test1>" +
                "           </aggregatedExampleList1Container1>" +
                "       </aggregatedExampleContainerList1>"+
                "       <aggregatedExampleContainerList1>"+
                "           <name1>Example2key1</name1>" +
                "           <name2>Example2key2</name2>" + 
                "       </aggregatedExampleContainerList1>"+
                "      </aggregatedExampleContainer1>" +
                "     <aggregatedExampleLeaf1>changedaggregatedLeaf1</aggregatedExampleLeaf1>" +
                "    </aggregatedExampleTopContainer>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                " </config>"+
                " </edit-config-payload>" +
                " </aggregated-edit-config>";
        String expectedErrMsg = "Could not find the node (schema-mount-test?revision=2018-01-03)aggregatedExampleLeaf5 in the YANG schema.";
        verifyMergeRequestFail(request1, expectedErrMsg);
    }

    private void verifyMergeRequestFail(String request, String errorMessage) throws NetconfMessageBuilderException {
        Element requestElement = DocumentUtils.stringToDocumentElement(request);
        List<Element> editConfigPayloadElements = DocumentUtils.getDirectChildElements(requestElement, "edit-config-payload", NetconfResources.NC_STACK_NS);
        try {
            getConfigAtnElement(editConfigPayloadElements);
            fail("should have been failed");
        } catch(Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
            assertEquals(errorMessage, e.getMessage());
        }
    }

    private void verifyMergeRequestSuccess(String request, String expectedMergeRequest, boolean hasMultipleConfigChild) throws Exception {
        Element actualMergeRequest = getMergedEditRequest(request, hasMultipleConfigChild);
        String actualMergeRequestStr = DocumentUtils.documentToPrettyString(actualMergeRequest);
        TestUtil.assertXMLStringEquals(expectedMergeRequest, actualMergeRequestStr);
    }

    private Element getMergedEditRequest(String request, boolean hasMultipleConfigChild) throws NetconfMessageBuilderException {
        Element requestElement = DocumentUtils.stringToDocumentElement(request);
        List<Element> editConfigPayloadElements = DocumentUtils.getDirectChildElements(requestElement, "edit-config-payload", NetconfResources.NC_STACK_NS);
        AggregatedTreeNode configAtn = getConfigAtnElement(editConfigPayloadElements);
        Element configElement = configAtn.toDom();
        if(!hasMultipleConfigChild) {
            return (Element) configElement.getChildNodes().item(0);
        } else {
            return configElement;
        }
    }

    private AggregatedTreeNode getConfigAtnElement(List<Element> editConfigPayloadElements) {
        AggregatedTreeNode configAtn = null;
        for(Element editConfigPayloadElement : editConfigPayloadElements) {
            Element configElement = DocumentUtils.getDirectChildElement(editConfigPayloadElement, "config");
            if(configAtn != null) {
                configAtn.add((Element) configElement);
            } else {
                configAtn = new AggregatedTreeNode(m_schemaRegistry, (Element) configElement, SchemaPath.ROOT);
            }
        }
        return configAtn;
    } 
}
