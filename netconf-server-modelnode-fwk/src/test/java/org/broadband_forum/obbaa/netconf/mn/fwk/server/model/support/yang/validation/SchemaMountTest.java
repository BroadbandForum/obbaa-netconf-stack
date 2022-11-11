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


import static junit.framework.TestCase.fail;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest.createRequestFromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.NetconfMessage;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RequestType;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcProcessException;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaMountTest extends AbstractSchemaMountTest {
    @Mock private ModelNode m_modelNode;

    static QName createQName(String localName){
        return QName.create("schema-mount-test", "2018-01-03", localName);
    }

    private final static QName SCHEMA_MOUNT = createQName("schemaMount");
    private final static QName SCHEMA_MOUNT1 = createQName("schemaMount1");
    private final static QName INTERFACES = QName.create("test-interfaces", "interfaces");

    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
        MockitoAnnotations.initMocks(this);
        SubSystem innerContainerSubsystem = new InnerContainerSubSystem();
        m_subSystemRegistry.register(COMPONENT_ID,INNER_CONTAINER_PATH,innerContainerSubsystem);
    }

    @Test
    public void testPlugAction() throws Exception {
        RpcRequestHandlerRegistry registry = new RpcRequestHandlerRegistryImpl();
        registry.registerMultiRpcRequestHandler(new DeviceMultiRpcRequestHandler());
        m_server.setRpcRequestHandlerRegistry(registry);
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <nested-predicates>"+
                "      <command-module>Buzz</command-module>" +
                "   </nested-predicates>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
        request =  "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
                " <action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"+ 
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <nested-predicates>" +
                "      <apollo11> " +
                "       <lunar-module>Neil</lunar-module>" +
                "      </apollo11>" +
                "     </nested-predicates>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>"+
                "</action>"+
                "</rpc>";
        ActionRequest validActionRequest = DocumentToPojoTransformer
                .getAction(DocumentUtils.stringToDocument(request));
        NetConfResponse response = new ActionResponse().setMessageId("1");
        try {
            // It should skip the constraint validation (lunar-module) and proceed.
            m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) response);
            fail("Action not implemented, so it should fail");
        } catch (RuntimeException ex){
            assertEquals("Action Not Implemented", ex.getMessage());
        }
    }

    @Test
    public void testPredicatesWithAndOperation1() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>ALU</leaf1>" + // This is the first part of 'and'. Correct value should be BBF. So I expect it to fail
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-operation>testingMust</predicates-with-and-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf1='BBF' and smt:name=/smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf3]/smt:leaf2 = ../firstLeaf");
    }

    @Test
    public void testPredicatesWithAndOperation2() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" + 
                "           <leaf3>testingMustNot</leaf3>" +// This is the second part of 'and'. Correct value should be predicates-with-and-operation value (that is testingMust, but I have given it as testingMustNot). So I expect it to fail
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-operation>testingMust</predicates-with-and-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf1='BBF' and smt:name=/smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf3]/smt:leaf2 = ../firstLeaf");
    }

    @Test
    public void testPredicatesWithAndOperation3() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + // Positive case
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-operation>testingMust</predicates-with-and-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithRelationOperator1() throws Exception {
        // Positive case
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf5>10</leaf5>" + // leaf5 < leaf6
                "           <leaf6>11</leaf6>" +
                "           <leaf2>Buzz</leaf2>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-relations-operator>testingMust</predicates-with-relations-operator>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithRelationOperator2() throws Exception {
        // Negative case
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf5>10</leaf5>" + // leaf5 < leaf6
                "           <leaf6>9</leaf6>" +
                "           <leaf2>Buzz</leaf2>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-relations-operator>testingMust</predicates-with-relations-operator>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf5 < /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf6]/smt:leaf2 = ../firstLeaf");
    }

    @Test @Ignore
    public void testPredicatesWithExtensionFunction() throws Exception {
        // Positive case
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf7>firstBit</leaf7>" +
                "           <leaf2>Buzz</leaf2>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicate-with-function>testingMust</predicate-with-function>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithAndOperationWithMultipleEntries1() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>ALU</leaf1>" + 
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "       <list1>"+
                "           <name>testingMust1</name>" +
                "           <leaf1>Bbf</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust2</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-operation>testingMust</predicates-with-and-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf1='BBF' and smt:name=/smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf3]/smt:leaf2 = ../firstLeaf");
    }

    @Test
    public void testPredicatesWithAndOperationWithMultipleEntries2() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>ALU</leaf1>" + 
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "       <list1>"+
                "           <name>testingMust1</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust2</leaf3>" +
                "       </list1>"+
                "       <list1>"+
                "           <name>testingMust2</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust2</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-operation>testingMust2</predicates-with-and-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    private void checkErrors(NetConfResponse response, String errorTag, String errorMsg) {
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals(errorTag, error.getErrorAppTag());
    }

    @Test
    public void testPredicatesWithOrOperation1() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMustNot</leaf3>" +// This is the second part of 'or'. Correct value should be predicates-with-and-operation value. So I expect it to pass
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-or-operation>testingMust</predicates-with-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithOrOperation2() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>ALU</leaf1>" +  // This is the first part of 'or'. Correct value should be BBF. So I expect it to pass
                "           <leaf2>Buzz</leaf2>" + 
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-or-operation>testingMust</predicates-with-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithOrOperation3() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + // Other case
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>ALU</leaf1>" +
                "           <leaf2>NoBuzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-or-operation>testingMust</predicates-with-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf1='BBF' or smt:name=/smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf3]/smt:leaf2 = ../firstLeaf");
    }

    @Test
    public void testPredicatesWithCoreOperation1() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust</name>" +
                "           <leaf1>BBF</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMustNot</leaf3>" +
                "           <leaf4>FNMS</leaf4>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-or-operation>testingMust</predicates-with-and-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }
    @Test
    public void testPredicatesWithCoreOperation2() throws Exception {   
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust1</name>" +
                "           <leaf1>ALU</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust1</leaf3>" +
                "           <leaf4>FNMS</leaf4>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-or-operation>testingMust1</predicates-with-and-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);
    }

    @Test
    public void testPredicatesWithCoreOperationNegativeCase() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <predicates-with-operation>"+
                "       <list1>"+
                "           <name>testingMust2</name>" +
                "           <leaf1>ALU</leaf1>" +
                "           <leaf2>Buzz</leaf2>" +
                "           <leaf3>testingMust</leaf3>" +
                "           <leaf4>BBA</leaf4>" +
                "       </list1>"+
                "      <firstLeaf>Buzz</firstLeaf>" +
                "      <predicates-with-and-or-operation>testingMust2</predicates-with-and-or-operation>" +
                "   </predicates-with-operation>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "Violate must constraints: /smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:leaf1='BBF' or smt:name=/smt:schemaMount/smt:predicates-with-operation/smt:list1[smt:name=current()]/smt:leaf3 and smt:leaf4='FNMS']/smt:leaf2 = ../firstLeaf");
    }

    @Test
    public void testMountProviderInfo() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        Element requestElement = DocumentUtils.stringToDocument(request).getDocumentElement();
        MountProviderInfo mountProviderInfo = SchemaRegistryUtil.getMountProviderInfo(requestElement , m_schemaRegistry);
        DataSchemaNode mountPathDataSchemaNode = mountProviderInfo.getMountedDataSchemaNode();
        assertEquals("schemaMountPoint", mountPathDataSchemaNode.getQName().getLocalName());
        Node mountPathXmlNodeFromRequest = mountProviderInfo.getMountedXmlNodeFromRequest();
        assertEquals("schemaMountPoint", mountPathXmlNodeFromRequest.getLocalName());
        SchemaRegistry mountedRegistry = mountProviderInfo.getMountedRegistry();
        assertEquals("schemaMountPoint", mountedRegistry.getMountPath().getLastComponent().getLocalName());
    }

    @Test
    public void testRemoveSchemaMountRootNode() throws Exception {

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <leaf1>leaf1</leaf1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\" xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">" +
                "     <leaf1>leaf1</leaf1>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);        
    }

    @Test
    public void testNestedPredicates() throws Exception {
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "     <interfaces xmlns=\"test-interfaces\">"+
                "      <interface>"+
                "       <name>testInterface</name>"+
                "       <channel-termination>"+
                "           <channel-pair-leaf>testInterface</channel-pair-leaf>"+
                "       </channel-termination>"+
                "     </interface>"+
                "    </interfaces>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "     <nested-predicates>"+
                "       <another-leaf>testInterface</another-leaf>"+
                "       <parent-ref>testInterface</parent-ref>"+
                "   </nested-predicates>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, requestXml, true);
    }

    @Test
    public void testProcessMissingDefaultData(){
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(SCHEMA_MOUNT_POINT_PATH);
        when(m_modelNode.hasSchemaMount()).thenReturn(true);
        SchemaRegistry mountRegistry = m_provider.getSchemaRegistry("PLUG-1.0");
        when(m_modelNode.getMountRegistry()).thenReturn(mountRegistry);
        when(m_modelNode.getSchemaRegistry()).thenReturn(mountRegistry);
    	AddDefaultDataInterceptor defaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry , m_schemaRegistry , m_expValidator);
    	defaultDataInterceptor.init();    	
    	EditContainmentNode editContainmentNode = new TestEditContainmentNode(SCHEMA_MOUNT_POINT, EditConfigOperations.CREATE, m_schemaRegistry);

    	EditContainmentNode interceptedContainmentNode = defaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
    	assertEquals(22, interceptedContainmentNode.getChildren().size());
    	assertNotNull(interceptedContainmentNode.getChildNode(SCHEMA_MOUNT));
    	assertNotNull(interceptedContainmentNode.getChildNode(SCHEMA_MOUNT1));
    	assertNotNull(interceptedContainmentNode.getChildNode(INTERFACES));    	
    }

    @Test
    public void testContainerWithMustAndCountNegativeCase() throws Exception {

        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang")), 
                Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-must-with-count xmlns=\"schema-mount-test\">" +
                        "     <list1>" +
                        "      <name>test</name>" + 
                        "     </list1>" +
                        "     <list1>" +
                        "      <name>test2</name>" + 
                        "     </list1>" +
                        "     </test-must-with-count>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        String errorMsg = "Violate must constraints: count(current()/../smt:list1) < 1";
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals("must-violation", error.getErrorAppTag());
    }

    @Test
    public void testContainerWithMustAndCountPositiveCase() throws Exception {

        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang")), 
                Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-must-with-count xmlns=\"schema-mount-test\" />" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);       

    }

    @Test
    public void testEditWithTwoMountRegistry() throws Exception {
        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang")), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-2.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount2nd xmlns=\"schema-2nd-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <leaf2>false</leaf2>" +
                        "    </schemaMount2nd>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
    }

    @Test
    public void testFindNodeWhenCacheHasWrongSMRegistry() throws Exception {
        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang"), 
                TestUtil.getByteSource("/datastorevalidatortest/yangs/sm-common-data-with-diff-conditions2.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/sm-plug2.yang")), 
                Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-2.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <plug2-container xmlns=\"sm-plug2\">"+
                        "    <p2>foo</p2>"+
                        "    <plug2-container-child>"+
                        "    	<leaf1>foo</leaf1>"+
                        "    </plug2-container-child>"+
                        "    </plug2-container>"+
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
        RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
        ModelNodeId parentId = new ModelNodeId(Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "urn:org:bbf2:pma:validation", "validation"), 
                new ModelNodeRdn(ModelNodeRdn.CONTAINER, "urn:org:bbf2:pma:validation", "xml-subtree"), new ModelNodeRdn("plugType", "urn:org:bbf2:pma:validation", "PLUG-2.0"), 
                new ModelNodeRdn(ModelNodeRdn.CONTAINER, "urn:org:bbf2:pma:validation", "schemaMountPoint"), new ModelNodeRdn(ModelNodeRdn.CONTAINER, "sm-plug2", "plug2-container")));
        SchemaPath nodeType = SchemaPath.create(true, QName.create("(sm-plug2?revision=2018-01-03)plug2-container"), QName.create("(sm-plug2?revision=2018-01-03)plug2-container-child"));
        ModelNode plugContainerModelNode = m_xmlSubtreeDSM.findNode(nodeType , new ModelNodeKeyBuilder().build(), parentId, mountRegistry2);
        ModelNode smNode = plugContainerModelNode.getParent();
        assertNotNull(smNode);
    }

    @Ignore
    @Test
    //FNMS-69232
    public void testEditWithTwoMountRegistryWIthDiffOrder() throws Exception {
        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-2nd-mount-test.yang"), 
                TestUtil.getByteSource("/datastorevalidatortest/yangs/sm-common-data-with-diff-conditions2.yang")), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-common-parent xmlns=\"sm-common-data-with-diff-conditions\">"+
                        "    <test-common>"+
                        "    <cmLeaf1>foo</cmLeaf1>"+
                        "    <cmLeaf2>true</cmLeaf2>"+
                        "    </test-common>"+
                        "    </test-common-parent>"+
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-2.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-common-parent xmlns=\"sm-common-data-with-diff-conditions\">"+
                        "    <test-common>"+
                        "    <cmLeaf1>test</cmLeaf1>"+
                        "    <cmLeaf2>true</cmLeaf2>"+
                        "    </test-common>"+
                        "    </test-common-parent>"+
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-2.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-common-parent xmlns=\"sm-common-data-with-diff-conditions\">"+
                        "    <test-common>"+
                        "    <cmLeaf1>test</cmLeaf1>"+
                        "    <cmLeaf2>true</cmLeaf2>"+
                        "    </test-common>"+
                        "    </test-common-parent>"+
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <test-common-parent xmlns=\"sm-common-data-with-diff-conditions\">"+
                        "    <test-common>"+
                        "    <cmLeaf1>foo</cmLeaf1>"+
                        "    <cmLeaf2>true</cmLeaf2>"+
                        "    </test-common>"+
                        "    </test-common-parent>"+
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
    }

    @Test
    public void erorPathTest() throws Exception {
        String requestString = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <uk:unknown xmlns:uk=\"unknown-ns\">" +
                        "     <uk:leaf1>leaf1</uk:leaf1>" +
                        "    </uk:unknown>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        EditConfigRequest request = createRequestFromString(requestString);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        String errorPath = response.getErrors().get(0).getErrorPath();
        assertEquals("/uk:unknown", errorPath);
    }

    @Test
    public void testDuplicateMountPoints() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf2</leaf1>" +
                        "     <container1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        String errorMsg = EditContainmentNode.DUPLICATE_ELEMENTS_FOUND + "(urn:org:bbf2:pma:validation?revision=2015-12-14)schemaMountPoint";
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals(EditContainmentNode.DATA_NOT_UNIQUE, error.getErrorAppTag());
    }

    @Test
    public void testSimpleEditGet() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        RequestScope.resetScope();
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>leaf1</smt:leaf1>"
                        + "       <smt:container1>"
                        + "        <smt:list1>"
                        + "         <smt:key>key</smt:key>"
                        + "         <smt:leafListMinElements>1</smt:leafListMinElements>"
                        + "        </smt:list1>"
                        + "       </smt:container1>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testDefaultIdentityRef() throws Exception {
        initialiseInterceptor();
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <leaf3>leaf3</leaf3>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        RequestScope.resetScope();
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "    <validation:currentChildTest/>"
                        + "    <validation:multiContainer>"
                        + "     <validation:level1>"
                        + "      <validation:level2/>"
                        + "     </validation:level1>"
                        + "    </validation:multiContainer>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:interface-container xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:ethernet xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardware xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwares xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"
                        + "      <copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"
                        + "      <if:interfaces xmlns:if=\"test-interfaces\"/>"
                        + "		 <test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        +"		 <smt:multicast xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>leaf1</smt:leaf1>"
                        + "       <smt:leaf3>leaf3</smt:leaf3>"
                        + "       <smt:id-ref-container>"
                        + "       <smt:identityref-type xmlns:if=\"test-interfaces\">if:english</smt:identityref-type>"
                        + "       <smt:local-identityref-type>smt:carrot</smt:local-identityref-type>"
                        + "       <smt:identityref-type2 xmlns:if=\"test-interfaces\">if:hindi</smt:identityref-type2>"
                        + "       <smt:identityref-type3>smt:carrot</smt:identityref-type3>"
                        + "       </smt:id-ref-container>"
                        + "      </smt:schemaMount>"
                        + "      <smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"
                        + "      <test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>"
                        + "      <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        +"       <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>" 
                        +"		 <smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"
                        + "     </validation:schemaMountPoint>"
                        + "     <validation:test-internal-request/>"
                        + "     <validation:testCurrentOnNonExistant>"
                        + "     <validation:container1/>"
                        + "     </validation:testCurrentOnNonExistant>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testWhenImpactWithMandatorySingleRequest(){
        initialiseInterceptor();
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <path1List>" +
                        "     <namePath1>one</namePath1>" +
                        "     <path1Container/>" +
                        "     </path1List>" +
                        "     <path2List>" +
                        "     <namePath2>one</namePath2>" +
                        "     <path2Container>" +
                        "     <trigger>whenMan</trigger>" +
                        "     </path2Container>" +
                        "     </path2List>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                ;
        NetConfResponse response = editConfigAsFalse(request);
        checkErrors(response, "instance-required", "Missing mandatory node - whenWithMan");
    }

    @Test
    public void testWhenImpactWithMandatoryMultipleRequests() throws IOException, SAXException {
        initialiseInterceptor();
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <path1List>" +
                        "     <namePath1>one</namePath1>" +
                        "     </path1List>" +
                        "     <path2List>" +
                        "     <namePath2>one</namePath2>" +
                        "     <path2Container>" +
                        "     <trigger>NotWhenMan</trigger>" +
                        "     </path2Container>" +
                        "     </path2List>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                ;
        editConfig(request);
//        verifyGet(request);
        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <path2List>" +
                        "     <namePath2>one</namePath2>" +
                        "     <path2Container>" +
                        "     <trigger>whenMan</trigger>" +
                        "     </path2Container>" +
                        "     </path2List>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
        ;
        NetConfResponse response = editConfigAsFalse(request);
        checkErrors(response, "instance-required", "Missing mandatory node - whenWithMan");
    }

    @Test
    public void testWhenDefaultIdentityRef() throws Exception {
        initialiseInterceptor();
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>myleaf1</leaf1>" +
                        "     <leaf3>leaf3</leaf3>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        RequestScope.resetScope();
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "    <validation:currentChildTest/>"
                        + "    <validation:multiContainer>"
                        + "     <validation:level1>"
                        + "      <validation:level2/>"
                        + "     </validation:level1>"
                        + "    </validation:multiContainer>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        + "      <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:interface-container xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:ethernet xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardware xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwares xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"
                        + "      <copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"
                        + "      <if:interfaces xmlns:if=\"test-interfaces\"/>"
                        + "		 <test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <smt:multicast xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>myleaf1</smt:leaf1>"
                        + "       <smt:leaf3>leaf3</smt:leaf3>"
                        + "       <smt:id-ref-container>"
                        + "       <smt:identityref-type xmlns:if=\"test-interfaces\">if:english</smt:identityref-type>"
                        + "       <smt:local-identityref-type>smt:carrot</smt:local-identityref-type>"
                        + "       <smt:local-whenidentityref-type>smt:carrot</smt:local-whenidentityref-type>"
                        + "       <smt:identityref-type1 xmlns:if=\"test-interfaces\">if:french</smt:identityref-type1>"
                        + "       <smt:identityref-type2 xmlns:if=\"test-interfaces\">if:hindi</smt:identityref-type2>"
                        + "       <smt:identityref-type3>smt:carrot</smt:identityref-type3>"
                        + "       </smt:id-ref-container>"
                        + "      </smt:schemaMount>"
                        + "      <smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"
                        + "      <test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>"
                        + "     <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>"
                        + "     </schemaMountPoint>"                        
                        + "     <validation:test-internal-request/>"
                        + "     <validation:testCurrentOnNonExistant>"
                        + "     <validation:container1/>"
                        + "     </validation:testCurrentOnNonExistant>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // update leaf1 value so impacts on when-identity-ref leaf
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>myleaf2</leaf1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        RequestScope.resetScope();
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "    <validation:currentChildTest/>"
                        + "    <validation:multiContainer>"
                        + "     <validation:level1>"
                        + "      <validation:level2/>"
                        + "     </validation:level1>"
                        + "    </validation:multiContainer>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        + "      <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:interface-container xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:ethernet xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardware xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwares xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"
                        + "      <copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"
                        + "      <if:interfaces xmlns:if=\"test-interfaces\"/>"
                        + "		 <test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        +"		 <smt:multicast xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>myleaf2</smt:leaf1>"
                        + "       <smt:leaf3>leaf3</smt:leaf3>"
                        + "       <smt:id-ref-container>"
                        + "       <smt:identityref-type xmlns:if=\"test-interfaces\">if:english</smt:identityref-type>"
                        + "       <smt:local-identityref-type>smt:carrot</smt:local-identityref-type>"
                        + "       <smt:identityref-type3>smt:carrot</smt:identityref-type3>"
                        + "       <smt:identityref-type2 xmlns:if=\"test-interfaces\">if:hindi</smt:identityref-type2>"
                        + "       </smt:id-ref-container>"
                        + "      </smt:schemaMount>"
                        + "      <smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"
                        + "      <test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>"
                        +" <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "      <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>"
                        + "     </schemaMountPoint>"
                        + "     <validation:test-internal-request/>"
                        + "     <validation:testCurrentOnNonExistant>"
                        + "     <validation:container1/>"
                        + "     </validation:testCurrentOnNonExistant>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        // update leaf3 value so impacts on container when-identity-ref
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf3>test</leaf3>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        RequestScope.resetScope();
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "    <validation:currentChildTest/>"
                        + "    <validation:multiContainer>"
                        + "     <validation:level1>"
                        + "      <validation:level2/>"
                        + "     </validation:level1>"
                        + "    </validation:multiContainer>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        + "      <smt:ber-tca-profiles xmlns:smt=\"schema-mount-test\"/>"
                        + "		 <smt:interface-container xmlns:smt=\"schema-mount-test\"/>"
                    	+ "      <smt:choice-case-container xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:classifiers xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:policies xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:ethernet xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:hardware xmlns:smt=\"schema-mount-test\"/>"
                    	+ "      <smt:hardwares xmlns:smt=\"schema-mount-test\"/>"
                    	+ "      <smt:hardwaresList xmlns:smt=\"schema-mount-test\"/>"
                        + "      <copyconfig:copy-config-container xmlns:copyconfig=\"copy-config-test\"/>"
                        + "      <if:interfaces xmlns:if=\"test-interfaces\"/>"
                        + "		 <test-mount:interfaces xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <test-mount:rollback-files xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <smt:multicast xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>myleaf2</smt:leaf1>"
                        + "       <smt:leaf3>test</smt:leaf3>"
                        + "      </smt:schemaMount>"
                        + "      <smt:schemaMount1 xmlns:smt=\"schema-mount-test\"/>"
                        + "      <smt-cm:test-common-parent xmlns:smt-cm=\"sm-common-data-with-diff-conditions\"/>"
                        + "      <test-mount:test-mount-action-container xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		<test-mount:test-action-with-mandatory xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "		 <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>"
                        +"       <test-mount:test-action-maxminvalidation-leaflistandlist xmlns:test-mount=\"urn:example:test-mount-action\"/>"
                        + "      <smt:test-must-with-count xmlns:smt=\"schema-mount-test\"/>"
                        + "     </schemaMountPoint>"
                        + "     <validation:test-internal-request/>"
                        + "     <validation:testCurrentOnNonExistant>"
                        + "     <validation:container1/>"
                        + "     </validation:testCurrentOnNonExistant>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testEnableContainerOnWhenCondition() throws Exception {
        ArgumentCaptor<EditConfigRequest> requestCaptor = ArgumentCaptor.forClass(EditConfigRequest.class);
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf2>true</leaf2>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String internalEditRequest = 
                "<rpc message-id=\"internal_edit:1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                        + "  <edit-config>"
                        + "   <target>"
                        + "    <running/>"
                        + "   </target>"
                        + "   <default-operation>merge</default-operation>"
                        + "   <test-option>set</test-option>"
                        + "   <error-option>stop-on-error</error-option>"
                        + "   <config>"
                        + "   <validation xmlns=\"urn:org:bbf2:pma:validation\">"
                        + "    <xml-subtree>"
                        + "  <plugType>PLUG-1.0</plugType>" 
                        + "     <schemaMountPoint>"
                        + "      <schemaMount xmlns=\"schema-mount-test\">"
                        + "       <enableContainer xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"
                        + "            <enableContainerChildLeaf xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">enableContainerLeaf-default-Value</enableContainerChildLeaf>\n"
                        + "       </enableContainer>"
                        + "      </schemaMount>"
                        + "     </schemaMountPoint>"
                        + "    </xml-subtree>"
                        + "   </validation>"
                        + "   </config>"
                        + "  </edit-config>"
                        + " </rpc>";

        Element internalEditRequest1Element = DocumentUtils.stringToDocument(internalEditRequest).getDocumentElement();

        verify(m_integrityService, times(3)).createInternalEditRequests(requestCaptor.capture(), any(NetconfClientInfo.class),
                any(DSValidationContext.class));
        List<EditConfigRequest> editRequests = requestCaptor.getAllValues();
        boolean request1Found = false, request2Found = false;
        for(EditConfigRequest editRequest : editRequests) {
            if(editRequest.getMessageId().equals("internal_edit:1")){
                TestUtil.assertXMLEquals(internalEditRequest1Element, editRequest.getRequestDocument().getDocumentElement());
                request1Found = true;
            }
        }
        assertEquals(3, editRequests.size());
        assertTrue(request1Found);
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:enableContainer>\n"
                        + "           <smt:enableContainerChildLeaf>enableContainerLeaf-default-Value</smt:enableContainerChildLeaf>\n"
                        + "       </smt:enableContainer>"
                        + "      <smt:leaf2>true</smt:leaf2>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);

        //disabling the leaf2, so enableContainer will get deleted
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf2>false</leaf2>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:schemaMountPoint>"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "      <smt:leaf2>false</smt:leaf2>"
                        + "      </smt:schemaMount>"
                        + "     </validation:schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testParentModelNodeDeletion() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <pae>" +
                        "      <port-capabilities> " +
                        "         <auth>true</auth>" +
                        "         <supp>false</supp>" +
                        "      </port-capabilities>" +
                        "      <authenticator> " +
                        "         <dummyLeaf>dummy</dummyLeaf>" +
                        "      </authenticator>" +
                        "     </pae>" +
                        "    </schemaMount>" +
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
                "               <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:leaf1>leaf1</smt:leaf1>\n" +
                "                  <smt:pae>\n" +
                "                     <smt:authenticator>\n" +
                "                        <smt:dummyLeaf>dummy</smt:dummyLeaf>\n" +
                "                     </smt:authenticator>\n" +
                "                     <smt:port-capabilities>\n" +
                "                        <smt:auth>true</smt:auth>\n" +
                "                        <smt:supp>false</smt:supp>\n" +
                "                     </smt:port-capabilities>\n" +
                "                  </smt:pae>\n" +
                "               </smt:schemaMount>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <pae>" +
                        "      <port-capabilities> " +
                        "         <auth>false</auth>" +
                        "         <supp>false</supp>" +
                        "      </port-capabilities>" +
                        "     </pae>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        editConfig(request);
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:leaf1>leaf1</smt:leaf1>\n" +
                "                  <smt:pae>\n" +
                "                     <smt:port-capabilities>\n" +
                "                        <smt:auth>false</smt:auth>\n" +
                "                        <smt:supp>false</smt:supp>\n" +
                "                     </smt:port-capabilities>\n" +
                "                  </smt:pae>\n" +
                "               </smt:schemaMount>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(response);
    }

    public void editGetWithConstraintTest() throws Exception {
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf1>key</leaf1>" +
                        "         <leaf2>20</leaf2>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                ;
        editConfig(request);
    }

    @Test
    public void testLeafRefConstraint() throws Exception {
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        //verify response has the changes
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "   <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:container1>"
                        +"       <smt:list1>"
                        +"        <smt:key>key</smt:key>"
                        +"        <smt:leaf3>20</smt:leaf3>"
                        +"        <smt:leaf4>20</smt:leaf4>"
                        + "       <smt:leafListMinElements>1</smt:leafListMinElements>"
                        +"       </smt:list1>"
                        +"      </smt:container1>"
                        +"     <smt:leaf1>leaf1</smt:leaf1>"
                        +"    </smt:schemaMount>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    } 

    @SuppressWarnings("unchecked")
    @Test
    public void testGetOnStateData() throws Exception {
        QName stateContainerQName = createQName("stateContainer");
        SchemaPath schemaPath = SchemaPath.create(true, stateContainerQName );
        SubSystem stateContainerSubSystem = mock(SubSystem.class);
        SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode.getModelNodeId());
        subSystemRegistry.register("test", schemaPath , stateContainerSubSystem );
        String result = 
                "<smt:stateContainer xmlns:smt=\"schema-mount-test\">"
                        +"       <smt:stateList>"
                        +"        <smt:keyLeaf>key</smt:keyLeaf>"
                        +"       </smt:stateList>"
                        +"      </smt:stateContainer>";
        Element resultElement = DocumentUtils.stringToDocument(result).getDocumentElement();
        doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {

            @Override
            public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation.getArguments()[0];
                ModelNodeId modelNodeId = map.keySet().iterator().next();
                Map<ModelNodeId, List<Element>> result = new HashMap<>();
                result.put(modelNodeId, Arrays.asList(resultElement));
                return result;
            }
        }).when(stateContainerSubSystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class),any(StateAttributeGetContext.class));
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String filter = "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"      <smt:stateContainer xmlns:smt=\"schema-mount-test\">"
                +"       <smt:stateList>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList>"
                +"      </smt:stateContainer>"
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>";

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +"      <smt:stateContainer xmlns:smt=\"schema-mount-test\">"
                        +"       <smt:stateList>"
                        +"        <smt:keyLeaf>key</smt:keyLeaf>"
                        +"       </smt:stateList>"
                        +"      </smt:stateContainer>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, filter, response);
    }

    @Test
    public void testMustConditionWithDifferentRootNodes() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>false</enabled>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse ncResponse = editConfigAsFalse(request); // Should fail due to must check
        assertEquals("A forwarder port with usage network-port must not be disabled.", ncResponse.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testMustConstraintCount_XPathWithDifferentSchemaRootNodes() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"  
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"  
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf1</if:name>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"          <smt:forwarding>"
                        +"              <smt:forwarder>"
                        +"                  <smt:name>forwarder1</smt:name>"
                        +"                  <smt:ports>"
                        +"                      <smt:port>"
                        +"                          <smt:name>port1</smt:name>"
                        +"                          <smt:sub-interface>intf1</smt:sub-interface>"
                        +"                      </smt:port>"
                        +"                  </smt:ports>"
                        +"              </smt:forwarder>"
                        +"          </smt:forwarding>"
                        +"          <smt:leaf1>leaf1</smt:leaf1>"
                        +"      </smt:schemaMount>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>true</enabled>" +
                        "         <ipv4-security xmlns=\"schema-mount-test\">"+
                        "           <prevent-ipv4-address-spoofing>true</prevent-ipv4-address-spoofing>"+ // evaluate count 
                        "         </ipv4-security>"+
                        "      </interface>" +
                        "     </interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // verify GET response
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"  
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"  
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf1</if:name>"
                        +"         <smt:ipv4-security xmlns:smt=\"schema-mount-test\">"
                        +"           <smt:prevent-ipv4-address-spoofing>true</smt:prevent-ipv4-address-spoofing>"
                        +"         </smt:ipv4-security>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"          <smt:forwarding>"
                        +"              <smt:forwarder>"
                        +"                  <smt:name>forwarder1</smt:name>"
                        +"                  <smt:ports>"
                        +"                      <smt:port>"
                        +"                          <smt:name>port1</smt:name>"
                        +"                          <smt:sub-interface>intf1</smt:sub-interface>"
                        +"                      </smt:port>"
                        +"                  </smt:ports>"
                        +"              </smt:forwarder>"
                        +"          </smt:forwarding>"
                        +"          <smt:leaf1>leaf1</smt:leaf1>"
                        +"      </smt:schemaMount>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMustConstraintCount_XPathWithDifferentSchemaRootNodes_Fail() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"  
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" 
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf1</if:name>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"  
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" 
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf2</if:name>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"          <smt:forwarding>"
                        +"              <smt:forwarder>"
                        +"                  <smt:name>forwarder1</smt:name>"
                        +"                  <smt:ports>"
                        +"                      <smt:port>"
                        +"                          <smt:name>port1</smt:name>"
                        +"                          <smt:sub-interface>intf1</smt:sub-interface>"
                        +"                      </smt:port>"
                        +"                  </smt:ports>"
                        +"              </smt:forwarder>"
                        +"          </smt:forwarding>"
                        +"          <smt:leaf1>leaf1</smt:leaf1>"
                        +"      </smt:schemaMount>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <enabled>true</enabled>" +
                        "         <ipv4-security xmlns=\"schema-mount-test\">"+
                        "           <prevent-ipv4-address-spoofing>true</prevent-ipv4-address-spoofing>"+ // count is 0 after evaluating xpath, so this request must be failed
                        "         </ipv4-security>"+
                        "      </interface>" +
                        "     </interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse ncResponse = editConfigAsFalse(request); // Should fail due to must constraint
        assertEquals("First attach the VLAN sub-interface to a forwarder", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='intf2']/smt:ipv4-security/smt:prevent-ipv4-address-spoofing", ncResponse.getErrors().get(0).getErrorPath());
        assertEquals("must-violation", ncResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, ncResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintCount_XPathWithDifferentSchemaRootNodes1() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <enabled>true</enabled>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n" 
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"  
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"  
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf1</if:name>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"  
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" 
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf2</if:name>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"          <smt:forwarding>"
                        +"              <smt:forwarder>"
                        +"                  <smt:name>forwarder1</smt:name>"
                        +"                  <smt:ports>"
                        +"                      <smt:port>"
                        +"                          <smt:name>port1</smt:name>"
                        +"                          <smt:sub-interface>intf1</smt:sub-interface>"
                        +"                      </smt:port>"
                        +"                  </smt:ports>"
                        +"              </smt:forwarder>"
                        +"          </smt:forwarding>"
                        +"          <smt:leaf1>leaf1</smt:leaf1>"
                        +"      </smt:schemaMount>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <enabled>true</enabled>" +
                        "         <ipv4-security xmlns=\"schema-mount-test\">"+
                        "           <prevent-ipv4-address-spoofing>false</prevent-ipv4-address-spoofing>"+
                        "         </ipv4-security>"+
                        "      </interface>" +
                        "     </interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        editConfig(request); // Should pass

        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        +"    <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        +"      <if:interfaces xmlns:if=\"test-interfaces\">"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n"
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf1</if:name>"
                        +"        </if:interface>"
                        +"        <if:interface>"
                        +"          <if:enabled>true</if:enabled>"
                        +"          <if:mybits>fourthBit</if:mybits>\n"
                        +"           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"
                        +"           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" 
                        +"          <intf-usage:interface-usage xmlns:intf-usage=\"bbf-interface-usage\">"
                        +"            <intf-usage:interface-usage>network-port</intf-usage:interface-usage>"
                        +"          </intf-usage:interface-usage>"
                        +"          <if:name>intf2</if:name>"
                        +"         <ipv4-security xmlns=\"schema-mount-test\">"
                        +"           <prevent-ipv4-address-spoofing>false</prevent-ipv4-address-spoofing>"
                        +"         </ipv4-security>"
                        +"        </if:interface>"
                        +"      </if:interfaces>"
                        +"      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"          <smt:forwarding>"
                        +"              <smt:forwarder>"
                        +"                  <smt:name>forwarder1</smt:name>"
                        +"                  <smt:ports>"
                        +"                      <smt:port>"
                        +"                          <smt:name>port1</smt:name>"
                        +"                          <smt:sub-interface>intf1</smt:sub-interface>"
                        +"                      </smt:port>"
                        +"                  </smt:ports>"
                        +"              </smt:forwarder>"
                        +"          </smt:forwarding>"
                        +"          <smt:leaf1>leaf1</smt:leaf1>"
                        +"      </smt:schemaMount>"
                        +"   </schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testConstraintPredicatesOnDifferentRootNodes() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf3</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf2</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "      <forwarder>" +
                        "        <name>forwarder2</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port3</name>" +
                        "          <sub-interface>intf3</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        //verify response has the changes
        String response =  
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" +
                        " <data>" +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "           <mybits>fourthBit</mybits>\n" + 
                        "           <mybitsdefaultValue>thirdBit</mybitsdefaultValue>\n"+
                        "           <mybitsoverridden>fourthBit</mybitsoverridden>\n" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf2</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "           <mybits>fourthBit</mybits>\n" + 
                        "           <mybitsdefaultValue>thirdBit</mybitsdefaultValue>\n" + 
                        "           <mybitsoverridden>fourthBit</mybitsoverridden>\n" +
                        "      </interface>" +
                        "      <interface> " +
                        "         <name>intf3</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>network-port</interface-usage>" +
                        "         </interface-usage>" +
                        "           <mybits>fourthBit</mybits>\n" + 
                        "           <mybitsdefaultValue>thirdBit</mybitsdefaultValue>\n" + 
                        "           <mybitsoverridden>fourthBit</mybitsoverridden>\n" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port1</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf2</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "      <forwarder>" +
                        "        <name>forwarder2</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port3</name>" +
                        "          <sub-interface>intf3</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"  +
                        "</data>"  +
                        "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testInvalidConstraintPredicatesOnDifferentRootNodes(){
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <interfaces xmlns=\"test-interfaces\">" +
                        "      <interface> " +
                        "         <name>intf1</name>" +
                        "         <interface-usage xmlns=\"bbf-interface-usage\">" +
                        "          <interface-usage>user-port</interface-usage>" +
                        "         </interface-usage>" +
                        "      </interface>" +
                        "     </interfaces>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <forwarding>" +
                        "      <forwarder>" +
                        "        <name>forwarder1</name>" +
                        "        <ports>" +
                        "         <port> " +
                        "          <name>port2</name>" +
                        "          <sub-interface>intf1</sub-interface>" +
                        "         </port> " +
                        "        </ports>" +
                        "      </forwarder>" +
                        "     </forwarding>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("A forwarder must have 1 port with usage network-port.", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:forwarding/smt:forwarder[smt:name='forwarder1']/smt:ports",
                ncResponse.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testMustConstraintLeafrefOnSameRootNode_ValidateMustConstraint() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test1</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "     <channelpair>" +
                        "      <channelgroup-ref>key</channelgroup-ref>" +
                        "     </channelpair>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";

        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("must reference a channelgroup", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:channelpair/smt:channelgroup-ref",
                ncResponse.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testMustConstraintLeafrefOnSameRootNode_ValidateLeafRef() throws Exception{
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "     <channelpair>" +
                        "      <channelgroup-ref>key</channelgroup-ref>" +
                        "     </channelpair>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                ;
        editConfig(request);

        //verify response has the changes
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "  <validation:plugType>PLUG-1.0</validation:plugType>"
                        +"    <validation:schemaMountPoint>"
                        +"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:container1>"
                        +"       <smt:list1>"
                        +"        <smt:key>key</smt:key>"
                        +"        <smt:leaf3>20</smt:leaf3>"
                        +"        <smt:leaf4>20</smt:leaf4>"
                        +"        <smt:leafListMinElements>1</smt:leafListMinElements>"
                        +"        <smt:type>test</smt:type>"
                        +"       </smt:list1>"
                        +"      </smt:container1>"
                        +"      <smt:channelpair>"
                        +"       <smt:channelgroup-ref>key</smt:channelgroup-ref>"
                        +"      </smt:channelpair>"
                        +"     <smt:leaf1>leaf1</smt:leaf1>"
                        +"    </smt:schemaMount>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "     <channelpair>" +
                        "      <channelgroup-ref>key1</channelgroup-ref>" +
                        "     </channelpair>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
        ;
        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("Dependency violated, 'key1' must exist", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:channelpair/smt:channelgroup-ref",
                ncResponse.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testMustConstraintLeafrefOnOtherRootNode_MustConstraint() throws Exception{
        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test1</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "    <schemaMount1 xmlns=\"schema-mount-test\">" +
                        "      <channelgroup-ref1>key</channelgroup-ref1>" +
                        "    </schemaMount1>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>";
        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("must reference a channelgroup", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount1/smt:channelgroup-ref1",
                ncResponse.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testMustConstraintLeafrefOnOtherRootNode_ValidateLeafRef() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "    <schemaMount1 xmlns=\"schema-mount-test\">" +
                        "      <channelgroup-ref1>key</channelgroup-ref1>" +
                        "    </schemaMount1>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        //verify response has the changes
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:container1>"
                        +"       <smt:list1>"
                        +"        <smt:key>key</smt:key>"
                        +"        <smt:leaf3>20</smt:leaf3>"
                        +"        <smt:leaf4>20</smt:leaf4>"
                        +"        <smt:leafListMinElements>1</smt:leafListMinElements>"
                        +"        <smt:type>test</smt:type>" 
                        +"       </smt:list1>"
                        +"      </smt:container1>"
                        +"     <smt:leaf1>leaf1</smt:leaf1>"
                        +"    </smt:schemaMount>"
                        +"    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:channelgroup-ref1>key</smt:channelgroup-ref1>"
                        +"    </smt:schemaMount1>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "    <schemaMount1 xmlns=\"schema-mount-test\">" +
                        "      <channelgroup-ref1>key1</channelgroup-ref1>" +
                        "    </schemaMount1>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse ncResponse = editConfigAsFalse(request);
        assertFalse(ncResponse.isOk());
        assertEquals("Dependency violated, 'key1' must exist", ncResponse.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount1/smt:channelgroup-ref1",
                ncResponse.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testCreateInnerChildNodeContainer() throws Exception{
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "         <type>test</type>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "    <schemaMount1 xmlns=\"schema-mount-test\">" +
                        "      <channelgroup-ref1>key</channelgroup-ref1>" +
                        "      <innerSchemaMount1>"   +
                        "       <innerSchemaMountLeaf>test</innerSchemaMountLeaf>" +
                        "      </innerSchemaMount1>"   +
                        "    </schemaMount1>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
        //verify response has the changes
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +"     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:container1>"
                        +"       <smt:list1>"
                        +"        <smt:key>key</smt:key>"
                        +"        <smt:leaf3>20</smt:leaf3>"
                        +"        <smt:leaf4>20</smt:leaf4>"
                        +"        <smt:leafListMinElements>1</smt:leafListMinElements>"
                        +"        <smt:type>test</smt:type>" 
                        +"       </smt:list1>"
                        +"      </smt:container1>"
                        +"     <smt:leaf1>leaf1</smt:leaf1>"
                        +"    </smt:schemaMount>"
                        +"    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"
                        +"      <smt:channelgroup-ref1>key</smt:channelgroup-ref1>"
                        +"      <smt:innerSchemaMount1>"
                        +"       <smt:innerSchemaMountLeaf>test</smt:innerSchemaMountLeaf>"
                        +"      </smt:innerSchemaMount1>"
                        +"    </smt:schemaMount1>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @Test
    public void testMountPoint() {
        Collection<DataSchemaNode> rootNodes = m_schemaRegistry.getRootDataSchemaNodes();
        for (DataSchemaNode rootNode:rootNodes) {
            if (rootNode.getQName().getLocalName().contains("mountPointTest")) {
                assertTrue(AnvExtensions.MOUNT_POINT.isExtensionIn(rootNode));
            }

            if (rootNode.getQName().getLocalName().contains("mountPointTest1")) {
                assertTrue(AnvExtensions.MOUNT_POINT.isExtensionIn(rootNode));
            }
        }
    }

    @Test
    public void testAugmentValidationInSchemaMount() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <leaf1>test1</leaf1>" + 
                "     <outer>" +
                "       <a>dummyValue</a>" + 
                "       <b>dummyValue</b>" + 
                "     </outer>" + 
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        assertEquals("Violate when constraints: leaf1 = 'test'", response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:outer",
                response.getErrors().get(0).getErrorPath());

        //positive case
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <leaf1>test</leaf1>" + 
                "     <outer>" +
                "       <a>dummyValue</a>" + 
                "       <b>dummyValue</b>" + 
                "     </outer>" + 
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";

        editConfig(request);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetRequest_FilterStateData() throws Exception {
        QName mountQName = createQName("schemaMount");
        SchemaPath mountSchemaPath = SchemaPath.create(true, mountQName);
        QName stateContainerQName = createQName("stateContainer1");
        SchemaPath stateContainerSchemaPath = AbstractValidationTestSetup.buildSchemaPath(mountSchemaPath,
                stateContainerQName);
        SubSystem stateContainerSubSystem = mock(SubSystem.class);
        SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode.getModelNodeId());
        subSystemRegistry.register("test", stateContainerSchemaPath, stateContainerSubSystem);
        String result =
                "	<smt:stateContainer1 xmlns:smt=\"schema-mount-test\">"
                        +"       <smt:stateList1>"
                        +"        	<smt:keyLeaf>key</smt:keyLeaf>"
                        +"       </smt:stateList1>"
                        +"   </smt:stateContainer1>";
        Element resultElement = DocumentUtils.stringToDocument(result).getDocumentElement();
        doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {
            @Override
            public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation
                        .getArguments()[0];
                ModelNodeId modelNodeId = map.keySet().iterator().next();
                Map<ModelNodeId, List<Element>> result = new HashMap<>();
                result.put(modelNodeId, Arrays.asList(resultElement));
                return result;
            }
        }).when(stateContainerSubSystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class), any(StateAttributeGetContext.class));
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String stateFilter = "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                +"   <validation:xml-subtree>"
                + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                +"    <validation:schemaMountPoint>"
                +"    <smt:schemaMount xmlns:smt=\"schema-mount-test\">" 
                +"      <smt:stateContainer1>"
                +"       <smt:stateList1>"
                +"        <smt:keyLeaf>key</smt:keyLeaf>"
                +"       </smt:stateList1>"
                +"      </smt:stateContainer1>"
                +"    </smt:schemaMount>" 
                +"   </validation:schemaMountPoint>"
                +"  </validation:xml-subtree>"
                +" </validation:validation>";

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        +" <data>"
                        +"  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +"    <smt:schemaMount xmlns:smt=\"schema-mount-test\">" 
                        +"      <smt:stateContainer1 xmlns=\"schema-mount-test\">"
                        +"       <smt:stateList1>"
                        +"        <smt:keyLeaf>key</smt:keyLeaf>"
                        +"       </smt:stateList1>"
                        +"      </smt:stateContainer1>"
                        +"    </smt:schemaMount>" 
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>"
                        +"</data>"
                        +"</rpc-reply>";

        verifyGet(m_server, m_clientInfo, stateFilter, response);
    }

    @Test
    public void leafRefConstraintWithDifferentRootNodes_InvalidRefValue() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1>" + 
                "       <profile>profile1</profile>" + 
                "	  </container1>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     	<profile>" +
                "       	<name>profile</name>" + 
                "     	</profile>" + 
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        assertEquals("Dependency violated, 'profile1' must exist", response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:profile",
                response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void leafRefWithDifferentRootNodes() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1>" + 
                "       <profile>testprofile</profile>" + 
                "	  </container1>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     	<profile>" +
                "       	<name>testprofile</name>" + 
                "     	</profile>" + 
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "     <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:container1>"+
                        "          <smt:profile>testprofile</smt:profile>"+
                        "        </smt:container1>"+
                        "     </smt:schemaMount>"+
                        "     <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:profile>"+
                        "          <smt:name>testprofile</smt:name>"+
                        "        </smt:profile>"+
                        "     </smt:schemaMount1>"+
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1 xc:operation=\"remove\">" + 
                "       <profile>testprofile</profile>" + 
                "	  </container1>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     	<profile xc:operation=\"remove\">" +
                "       	<name>testprofile</name>" + 
                "     	</profile>" + 
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);
    }

    @Test
    public void leafRefWithDifferentRootNodes_RemoveWithoutRefValue() throws Exception {

        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1>" + 
                "       <profile>testprofile</profile>" + 
                "	  </container1>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     	<profile>" +
                "       	<name>testprofile</name>" + 
                "     	</profile>" + 
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     	<profile xc:operation=\"remove\">" +
                "       	<name>testprofile</name>" + 
                "     	</profile>" + 
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";

        NetConfResponse response = editConfigAsFalse(request);
        assertFalse(response.isOk());
        assertEquals("Dependency violated, 'testprofile' must exist", response.getErrors().get(0).getErrorMessage());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:profile",
                response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testGetForStateContainers() throws Exception {

        //create element1 with some configuration under mount point
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <dummyList>" +
                        "     <name>element1</name>" +
                        "   </dummyList>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        //create element2
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <dummyList>" +
                        "     <name>element2</name>" +
                        "   </dummyList>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "  <data>"
                        + "   <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        + "    <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        + "     <validation:dummyList>" 
                        + "      <validation:name>element1</validation:name>" 
                        + "      <validation:innerContainer>" 
                        + "       <validation:state>reachable</validation:state>" 
                        + "      </validation:innerContainer>" 
                        + "     </validation:dummyList>" 
                        + "     <validation:dummyList>" 
                        + "      <validation:name>element2</validation:name>" 
                        + "      <validation:innerContainer>" 
                        + "       <validation:state>notReachable</validation:state>" 
                        + "      </validation:innerContainer>" 
                        + "     </validation:dummyList>" 
                        + "     <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"
                        + "      <smt:schemaMount xmlns:smt=\"schema-mount-test\">"
                        + "       <smt:leaf1>leaf1</smt:leaf1>"
                        + "       <smt:container1>"
                        + "        <smt:list1>"
                        + "         <smt:key>key</smt:key>"
                        + "         <smt:leafListMinElements>1</smt:leafListMinElements>"
                        + "        </smt:list1>"
                        + "       </smt:container1>"
                        + "      </smt:schemaMount>"
                        + "     </schemaMountPoint>"
                        + "    </validation:xml-subtree>"
                        + "   </validation:validation>"
                        + "  </data>"
                        + " </rpc-reply>"
                        ;
        verifyGet(m_server, m_clientInfo, response);
    }

    /*
     * Added to test proper displaying of State data
     */
    private class InnerContainerSubSystem extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            Document doc = DocumentUtils.createDocument();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                ModelNodeId modelNodeId = entry.getKey();
                List<Element> stateElements = new ArrayList<>();
                String elementName = modelNodeId.getRdns().get((modelNodeId.getRdns().size() - 1)).getRdnValue();
                Element innerContainerElement = doc.createElementNS(VALIDATION_NS,
                        getPrefixedLocalName("validation", INNER_CONTAINER_QNAME.getLocalName()));
                Element stateLeafElement = doc.createElementNS(VALIDATION_NS,
                        getPrefixedLocalName("validation", "state"));
                if (elementName.equals("element1")) {
                    stateLeafElement.setTextContent("reachable");
                } else if (elementName.equals("element2")) {
                    stateLeafElement.setTextContent("notReachable");
                }
                innerContainerElement.appendChild(stateLeafElement);
                stateElements.add(innerContainerElement);
                stateInfo.put(modelNodeId, stateElements);
            }
            return stateInfo;
        }
    }

    private static String getPrefixedLocalName(String prefix, String localname) {
        return prefix + ":" + localname;
    }

    @Test
    public void testInternalEditRequest_WhenConstraintWithDefault() throws Exception {

        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1>" + 
                "       <test-auth-fail>bbf</test-auth-fail>" +
                "	  </container1>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:container1>"+
                        "			<smt:test-auth-fail>bbf</smt:test-auth-fail>"+
                        "				<smt:trap>"+
                        "					<smt:auth-fail>test</smt:auth-fail>"+
                        "				</smt:trap>"+
                        "        </smt:container1>"+
                        "     </smt:schemaMount>"+
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, response);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetWithFilterOnListStateData() throws Exception {

        QName testStateList = createQName("testStateList");
        SchemaPath schemaPath = SchemaPath.create(true, testStateList );
        SubSystem testStateListSubsystem = mock(SubSystem.class);
        SubSystemRegistry subSystemRegistry = m_provider.getSubSystemRegistry(m_modelNode.getModelNodeId());
        subSystemRegistry.register("dummyId", schemaPath , testStateListSubsystem );

        String entry1 =
                "  <smt:testStateList xmlns:smt=\"schema-mount-test\">"
                        + "    <smt:name>testName1</smt:name>"
                        + "      <smt:dummy-leaf1>dummy1</smt:dummy-leaf1>"
                        + "  </smt:testStateList>";

        String entry2 =
                "  <smt:testStateList xmlns:smt=\"schema-mount-test\">"
                        + "    <smt:name>testName2</smt:name>"
                        + "      <smt:dummy-leaf1>dummy2</smt:dummy-leaf1>"
                        + "  </smt:testStateList>";

        Element entry1Element = DocumentUtils.stringToDocument(entry1).getDocumentElement();
        Element entry2Element = DocumentUtils.stringToDocument(entry2).getDocumentElement();
        List<Element> resultElements = new ArrayList<>();
        resultElements.add(entry1Element);
        resultElements.add(entry2Element);

        doAnswer(new Answer<Map<ModelNodeId, List<Element>>>() {

            @Override
            public Map<ModelNodeId, List<Element>> answer(InvocationOnMock invocation) throws Throwable {
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = (Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>) invocation.getArguments()[0];
                ModelNodeId modelNodeId = map.keySet().iterator().next();
                Map<ModelNodeId, List<Element>> result = new HashMap<>();
                result.put(modelNodeId, resultElements);
                return result;
            }
        }).when(testStateListSubsystem).retrieveStateAttributes(anyMap(), any(NetconfQueryParams.class),any(StateAttributeGetContext.class));

        String request =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "  <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" +
                        "     <leaf1>leaf1</leaf1>" +
                        "     <container1>" +
                        "      <list1> " +
                        "         <key>key</key>" +
                        "         <leaf3>20</leaf3>" +
                        "         <leaf4>20</leaf4>" +
                        "         <leafListMinElements>1</leafListMinElements>" +
                        "      </list1>" +
                        "     </container1>" +
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        String filterOnStateListKey = 
                " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                        +"   <validation:xml-subtree>"
                        + "     <validation:plugType>PLUG-1.0</validation:plugType>" 
                        +"    <validation:schemaMountPoint>"
                        +      "<smt:testStateList xmlns:smt=\"schema-mount-test\">"
                        +"        <smt:name />"
                        +"      </smt:testStateList>"
                        +"   </validation:schemaMountPoint>"
                        +"  </validation:xml-subtree>"
                        +" </validation:validation>";

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\"> " +
                        " <data> " +
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"> " +
                        "   <validation:xml-subtree> " +
                        "     <validation:plugType>PLUG-1.0</validation:plugType>" + 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\"> " +
                        "     <smt:testStateList xmlns:smt=\"schema-mount-test\"> " +
                        "      <smt:dummy-leaf1>dummy1</smt:dummy-leaf1> " +
                        "      <smt:name>testName1</smt:name> " +
                        "     </smt:testStateList> " +
                        "     <smt:testStateList xmlns:smt=\"schema-mount-test\"> " +
                        "      <smt:dummy-leaf1>dummy2</smt:dummy-leaf1> " +
                        "      <smt:name>testName2</smt:name> " +
                        "     </smt:testStateList> " +
                        "    </schemaMountPoint> " +
                        "   </validation:xml-subtree> " +
                        "  </validation:validation> " +
                        " </data> " +
                        "</rpc-reply>";

        verifyGet(m_server, m_clientInfo, filterOnStateListKey, response);
    }

    private String getEditRequest(String choiceRequest){
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <container1>" 
                + choiceRequest +
                "     </container1>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        return request;
    }

    private String getResponse(String choiceresponse){
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:container1>"+
                        choiceresponse +
                        "        </smt:container1>"+
                        "     </smt:schemaMount>"+
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";
        return response;
    }

    @Test
    public void testLeafRefWithChoiceCase() throws Exception {
        String requestXml = 
                "<choice-container>"
                        +" <interface>"
                        +"  <name>if1</name>"
                        +"  <tm-root>"
                        +"      <scheduler-node>"
                        +"          <name>test</name>"
                        +"          <scheduling-level>10</scheduling-level>"
                        +"      </scheduler-node>"
                        +"      <child-scheduler-nodes>"
                        +"          <name>test</name>"
                        +"      </child-scheduler-nodes>"
                        +"  </tm-root>"
                        +"  </interface>"
                        + "</choice-container>";

        editConfig(getEditRequest(requestXml));

        String responseXml =
                "<smt:choice-container>"
                        +"  <smt:interface>"
                        +"      <smt:name>if1</smt:name>"
                        +"      <smt:tm-root>"
                        +"          <smt:child-scheduler-nodes>"
                        +"              <smt:name>test</smt:name>"
                        +"          </smt:child-scheduler-nodes>"
                        +"          <smt:scheduler-node>"
                        +"              <smt:name>test</smt:name>"
                        +"              <smt:scheduling-level>10</smt:scheduling-level>"
                        +"          </smt:scheduler-node>"
                        +"      </smt:tm-root>"
                        +"  </smt:interface>"
                        +" </smt:choice-container>";
        verifyGet(getResponse(responseXml));

        // invalid leaf-ref value
        requestXml = 
                "<choice-container>"
                        +" <interface>"
                        +"  <name>if1</name>"
                        +"  <tm-root>"
                        +"      <scheduler-node>"
                        +"          <name>test1</name>"
                        +"          <scheduling-level>10</scheduling-level>"
                        +"      </scheduler-node>"
                        +"      <child-scheduler-nodes>"
                        +"          <name>test2</name>"
                        +"      </child-scheduler-nodes>"
                        +"  </tm-root>"
                        +"  </interface>"
                        + "</choice-container>";

        NetConfResponse response = editConfigAsFalse(getEditRequest(requestXml));
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:choice-container/smt:interface[smt:name='if1']/smt:tm-root/smt:child-scheduler-nodes[smt:name='test2']/smt:name", response.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, 'test2' must exist", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testLeafRefWithChoiceCase_MissingMandatoryNode() throws Exception {
        // missing mandatory node
        String requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node>"
                +"          <name>test</name>"
                +"      </scheduler-node>"
                +"      <child-scheduler-nodes>"
                +"          <name>test</name>"
                +"      </child-scheduler-nodes>"
                +"  </tm-root>"
                +"  </interface>"
                + "</choice-container>";

        NetConfResponse response = editConfigAsFalse(getEditRequest(requestXml));
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:choice-container/smt:interface[smt:name='if1']/smt:tm-root/smt:scheduler-node[smt:name='test']/smt:scheduling-level", response.getErrors().get(0).getErrorPath());
        assertEquals("Missing mandatory node - scheduling-level", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testLeafRefWithChoiceCase_RemoveReferenceNode() throws Exception {
        String requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node>"
                +"          <name>test</name>"
                +"          <scheduling-level>10</scheduling-level>"
                +"      </scheduler-node>"
                +"      <child-scheduler-nodes>"
                +"          <name>test</name>"
                +"      </child-scheduler-nodes>"
                +"      <scheduler-node>"
                +"          <name>test1</name>"
                +"          <scheduling-level>12</scheduling-level>"
                +"      </scheduler-node>"
                +"      <child-scheduler-nodes>"
                +"          <name>test1</name>"
                +"      </child-scheduler-nodes>"
                +"  </tm-root>"
                +"  </interface>"
                + "</choice-container>";

        editConfig(getEditRequest(requestXml));

        String responseXml = "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"          <smt:child-scheduler-nodes>"
                +"              <smt:name>test</smt:name>"
                +"          </smt:child-scheduler-nodes>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test</smt:name>"
                +"              <smt:scheduling-level>10</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"          <smt:child-scheduler-nodes>"
                +"              <smt:name>test1</smt:name>"
                +"          </smt:child-scheduler-nodes>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test1</smt:name>"
                +"              <smt:scheduling-level>12</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;

        verifyGet(getResponse(responseXml));

        // Remove valid leaf-ref case
        requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <child-scheduler-nodes xc:operation=\"delete\">"
                +"          <name>test1</name>"
                +"      </child-scheduler-nodes>"
                +"  </tm-root>"
                +" </interface>"
                +"</choice-container>";

        editConfig(getEditRequest(requestXml));

        responseXml = "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"          <smt:child-scheduler-nodes>"
                +"              <smt:name>test</smt:name>"
                +"          </smt:child-scheduler-nodes>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test</smt:name>"
                +"              <smt:scheduling-level>10</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test1</smt:name>"
                +"              <smt:scheduling-level>12</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;
        verifyGet(getResponse(responseXml));

        // Remove referenced node
        requestXml = "<choice-container >"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node xc:operation=\"delete\">"
                +"          <name>test</name>"
                +"          <scheduling-level>10</scheduling-level>"
                +"      </scheduler-node>"
                +"  </tm-root>"
                +" </interface>"
                +"</choice-container>";

        NetConfResponse response = editConfigAsFalse(getEditRequest(requestXml));
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:choice-container/smt:interface[smt:name='if1']/smt:tm-root/smt:child-scheduler-nodes[smt:name='test']/smt:name", response.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, 'test' must exist", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testLeafRefWithNestedChoiceCase() throws Exception {
        String requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node>"
                +"          <name>test</name>"
                +"          <scheduling-level>10</scheduling-level>"
                +"          <child-scheduler-nodes>"
                +"              <name>test1</name>"
                +"          </child-scheduler-nodes>"
                +"      </scheduler-node>"
                +"      <scheduler-node>"
                +"          <name>test1</name>"
                +"          <scheduling-level>11</scheduling-level>"
                +"      </scheduler-node>"
                +"  </tm-root>"
                +"  </interface>"
                + "</choice-container>";

        editConfig(getEditRequest(requestXml));

        String responseXml = "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test</smt:name>"
                +"              <smt:scheduling-level>10</smt:scheduling-level>"
                +"              <smt:child-scheduler-nodes>"
                +"                  <smt:name>test1</smt:name>"
                +"              </smt:child-scheduler-nodes>"
                +"          </smt:scheduler-node>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test1</smt:name>"
                +"              <smt:scheduling-level>11</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;
        verifyGet(getResponse(responseXml));

        // remove leaf-ref referenced node
        requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node xc:operation=\"delete\">"
                +"          <name>test1</name>"
                +"      </scheduler-node>"
                +"  </tm-root>"
                +" </interface>"
                +"</choice-container>";

        NetConfResponse response = editConfigAsFalse(getEditRequest(requestXml));
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:container1/smt:choice-container/smt:interface[smt:name='if1']/smt:tm-root/smt:scheduler-node[smt:name='test']/smt:child-scheduler-nodes[smt:name='test1']/smt:name", response.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, 'test1' must exist", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    public void testLeafRefWithChoiceCase_RemoveValidReferenceNode() throws Exception {
        String requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node>"
                +"          <name>test</name>"
                +"          <scheduling-level>10</scheduling-level>"
                +"      </scheduler-node>"
                +"      <child-scheduler-nodes>"
                +"          <name>test</name>"
                +"      </child-scheduler-nodes>"
                +"  </tm-root>"
                +"  </interface>"
                + "</choice-container>";

        editConfig(getEditRequest(requestXml));

        String responseXml = "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"          <smt:child-scheduler-nodes>"
                +"              <smt:name>test</smt:name>"
                +"          </smt:child-scheduler-nodes>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test</smt:name>"
                +"              <smt:scheduling-level>10</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;

        verifyGet(getResponse(responseXml));

        //remove referenced node
        requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <child-scheduler-nodes xc:operation=\"delete\">"
                +"          <name>test</name>"
                +"      </child-scheduler-nodes>"
                +"  </tm-root>"
                +" </interface>"
                +"</choice-container>";

        editConfig(getEditRequest(requestXml));

        responseXml =  "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"          <smt:scheduler-node>"
                +"              <smt:name>test</smt:name>"
                +"              <smt:scheduling-level>10</smt:scheduling-level>"
                +"          </smt:scheduler-node>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;

        verifyGet(getResponse(responseXml));

        // remove actual node
        requestXml = "<choice-container>"
                +" <interface>"
                +"  <name>if1</name>"
                +"  <tm-root>"
                +"      <scheduler-node xc:operation=\"delete\">"
                +"          <name>test</name>"
                +"      </scheduler-node>"
                +"  </tm-root>"
                +" </interface>"
                +"</choice-container>";

        editConfig(getEditRequest(requestXml));

        responseXml = "<smt:choice-container>"
                +"  <smt:interface>"
                +"      <smt:name>if1</smt:name>"
                +"      <smt:tm-root>"
                +"      </smt:tm-root>"
                +"  </smt:interface>"
                +" </smt:choice-container>"
                ;

        verifyGet(getResponse(responseXml));
    }

    /**
     * Below UTs to cover leafref validation for leaf-list schema nodes under schema mount
     */

    @Test
    public void testLeafRefWithLeafList() throws Exception {

        // create leaf-list nodes
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <interface-list>" +
                "      <name>if</name>"+
                "      <port-layer>PORT2</port-layer>" +
                "     </interface-list>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     <hardware>" +
                "      <name>PORT1</name>"+
                "     </hardware>"+
                "     <hardware>" +
                "      <name>PORT2</name>"+
                "     </hardware>"+
                "     <hardware>" +
                "      <name>PORT3</name>"+
                "     </hardware>"+
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        //verify the response
        String responseXml = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:interface-list>"+
                        "          <smt:name>if</smt:name>"+
                        "          <smt:port-layer>PORT2</smt:port-layer>"+
                        "        </smt:interface-list>"+
                        "     </smt:schemaMount>"+
                        "    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">" + 
                        "     <smt:hardware>" +
                        "      <smt:name>PORT1</smt:name>"+
                        "     </smt:hardware>"+
                        "     <smt:hardware>" +
                        "      <smt:name>PORT2</smt:name>"+
                        "     </smt:hardware>"+
                        "     <smt:hardware>" +
                        "      <smt:name>PORT3</smt:name>"+
                        "     </smt:hardware>"+
                        "    </smt:schemaMount1>" +
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(responseXml);

        // Add two more leaf-list nodes
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <interface-list>" +
                "      <name>if</name>"+
                "      <port-layer>PORT1</port-layer>" +
                "      <port-layer>PORT3</port-layer>" +
                "     </interface-list>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        responseXml = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:interface-list>"+
                        "          <smt:name>if</smt:name>"+
                        "          <smt:port-layer>PORT2</smt:port-layer>"+
                        "          <smt:port-layer>PORT1</smt:port-layer>"+
                        "          <smt:port-layer>PORT3</smt:port-layer>"+
                        "        </smt:interface-list>"+
                        "     </smt:schemaMount>"+
                        "    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">" + 
                        "     <smt:hardware>" +
                        "      <smt:name>PORT1</smt:name>"+
                        "     </smt:hardware>"+
                        "     <smt:hardware>" +
                        "      <smt:name>PORT2</smt:name>"+
                        "     </smt:hardware>"+
                        "     <smt:hardware>" +
                        "      <smt:name>PORT3</smt:name>"+
                        "     </smt:hardware>"+
                        "    </smt:schemaMount1>" +
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(responseXml);
    }

    @Test
    public void testLeafRefWithLeafList_RemoveLeafRefNode() throws Exception {
        //create leaf-list nodes
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" + 
                "     <interface-list>" +
                "      <name>if</name>"+
                "      <port-layer>PORT2</port-layer>" +
                "     </interface-list>"+
                "    </schemaMount>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     <hardware>" +
                "      <name>PORT1</name>"+
                "     </hardware>"+
                "     <hardware>" +
                "      <name>PORT2</name>"+
                "     </hardware>"+
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        String responseXml = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:interface-list>"+
                        "          <smt:name>if</smt:name>"+
                        "          <smt:port-layer>PORT2</smt:port-layer>"+
                        "        </smt:interface-list>"+
                        "     </smt:schemaMount>"+
                        "    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">" + 
                        "     <smt:hardware>" +
                        "      <smt:name>PORT1</smt:name>"+
                        "     </smt:hardware>"+
                        "     <smt:hardware>" +
                        "      <smt:name>PORT2</smt:name>"+
                        "     </smt:hardware>"+
                        "    </smt:schemaMount1>" +
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(responseXml);

        // remove leafref value which is not used in leaf-list node
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     <hardware xc:operation=\"delete\">" +
                "      <name>PORT1</name>"+
                "     </hardware>"+
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";

        editConfig(request);

        responseXml = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        " <data>"+
                        "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "   <validation:xml-subtree>"+
                        "    <validation:plugType>PLUG-1.0</validation:plugType>"+ 
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "        <smt:interface-list>"+
                        "          <smt:name>if</smt:name>"+
                        "          <smt:port-layer>PORT2</smt:port-layer>"+
                        "        </smt:interface-list>"+
                        "     </smt:schemaMount>"+
                        "    <smt:schemaMount1 xmlns:smt=\"schema-mount-test\">" + 
                        "     <smt:hardware>" +
                        "      <smt:name>PORT2</smt:name>"+
                        "     </smt:hardware>"+
                        "    </smt:schemaMount1>" +
                        "    </schemaMountPoint>"+
                        "   </validation:xml-subtree>"+
                        "  </validation:validation>"+
                        " </data>"+
                        "</rpc-reply>";

        verifyGet(responseXml);

        // remove leafref value which is used in leaf-list node
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount1 xmlns=\"schema-mount-test\">" + 
                "     <hardware xc:operation=\"delete\">" +
                "      <name>PORT2</name>"+
                "     </hardware>"+
                "    </schemaMount1>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";

        // verify the error details
        NetConfResponse response = editConfigAsFalse(request);
        assertEquals(1,response.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:interface-list[smt:name='if']/smt:port-layer", response.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, 'PORT2' must exist", response.getErrors().get(0).getErrorMessage());
        assertEquals("instance-required", response.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, response.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testPrefixInAugmentNodeOfDifferentModule() throws Exception{
        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/test-interfaces.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/schema-mount-test.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/interface-ref.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/bbf-interface-usage.yang"), TestUtil.getByteSource(
                        "/datastorevalidatortest/yangs/mountRegistry2/bbf-xpon-if-type.yang"), TestUtil.getByteSource(
                                "/datastorevalidatortest/yangs/mountRegistry2/bbf-sub-interfaces.yang")), Collections.emptySet()
                , Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <xml-subtree>"
                + "  <plugType>PLUG-2.0</plugType>"
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface>"
                + "     <name>test</name>"
                + "		<type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</type>"
                + "     <ssm-out-profile-ref xmlns=\"bbf-sub-interfaces\">testContext</ssm-out-profile-ref>"
                + "     </interface>"
                + "     <interface>"
                + "     <name>test2</name>"
                + "		<type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</type>"
                + "     <ssm-out-profile-ref xmlns=\"bbf-sub-interfaces\">testContext</ssm-out-profile-ref>"
                + "     </interface>"
                + "    </interfaces>"
                + "		<tconts-config xmlns=\"https://interface-ref\"/>"
                + "   </schemaMountPoint>"
                + "  </xml-subtree>"
                + " </validation>";
        editConfig(requestXml);

        String responseXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                        "   <data>\n" +
                        "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                        "         <validation:xml-subtree>\n" +
                        "            <validation:plugType>PLUG-2.0</validation:plugType>\n" +
                        "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                        "               <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                        "                  <if:interface>\n" +
                        "                     <if:name>test</if:name>\n" +
                        "                     <bbf-subif:ssm-out-profile-ref " +
                        "xmlns:bbf-subif=\"bbf-sub-interfaces\">testContext</bbf-subif:ssm-out-profile-ref>\n" +
                        "                     <if:type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</if" +
                        ":type>\n" +
                        "                  </if:interface>\n" +
                        "                  <if:interface>\n" +
                        "                     <if:name>test2</if:name>\n" +
                        "                     <bbf-subif:ssm-out-profile-ref " +
                        "xmlns:bbf-subif=\"bbf-sub-interfaces\">testContext</bbf-subif:ssm-out-profile-ref>\n" +
                        "                     <if:type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</if" +
                        ":type>\n" +
                        "                  </if:interface>\n" +
                        "               </if:interfaces>\n" +
                        "               <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>\n" +
                        "            </schemaMountPoint>\n" +
                        "         </validation:xml-subtree>\n" +
                        "      </validation:validation>\n" +
                        "   </data>\n" +
                        "</rpc-reply>";

        verifyGet(responseXml);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">"
                + "  <xml-subtree>"
                + "  <plugType>PLUG-2.0</plugType>"
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"remove\">"
                + "     <name>test</name>"
                + "		<type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</type>"
                + "     <ssm-out-profile-ref xmlns=\"bbf-sub-interfaces\">testContext</ssm-out-profile-ref>"
                + "     </interface>"
                + "    </interfaces>"
                + "		<tconts-config xmlns=\"https://interface-ref\"/>"
                + "   </schemaMountPoint>"
                + "  </xml-subtree>"
                + " </validation>";
        editConfig(requestXml);

        responseXml =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                        "   <data>\n" +
                        "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                        "         <validation:xml-subtree>\n" +
                        "            <validation:plugType>PLUG-2.0</validation:plugType>\n" +
                        "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                        "               <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                        "                  <if:interface>\n" +
                        "                     <if:name>test2</if:name>\n" +
                        "                     <bbf-subif:ssm-out-profile-ref " +
                        "xmlns:bbf-subif=\"bbf-sub-interfaces\">testContext</bbf-subif:ssm-out-profile-ref>\n" +
                        "                     <if:type xmlns:bbf-xponift=\"urn:bbf:yang:bbf-xpon-if-type\">bbf-xponift:channel-pair</if" +
                        ":type>\n" +
                        "                  </if:interface>\n" +
                        "               </if:interfaces>\n" +
                        "               <if-ref:tconts-config xmlns:if-ref=\"https://interface-ref\"/>\n" +
                        "            </schemaMountPoint>\n" +
                        "         </validation:xml-subtree>\n" +
                        "      </validation:validation>\n" +
                        "   </data>\n" +
                        "</rpc-reply>";

        verifyGet(responseXml);
    }


    @Test
    public void testCorrectValidationContext() throws Exception{
        SchemaRegistry mountRegistry2 = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/test-interfaces.yang"), 
                TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/schema-mount-test.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/interface-ref.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/mountRegistry2/bbf-interface-usage.yang")), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry2.setName("PLUG-2.0");
        makeSetup(mountRegistry2);

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "  <xml-subtree>"
                + "  <plugType>PLUG-1.0</plugType>" 
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface>"
                + "     <name>testContext</name>"
                + "		<type>ieee80211</type>"
                + "     </interface>"
                + "    </interfaces>"
                + "		<tconts-config xmlns=\"https://interface-ref\"/>"
                + "   </schemaMountPoint>" 
                + "  </xml-subtree>"
                + "  <xml-subtree>"
                + "  <plugType>PLUG-2.0</plugType>" 
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface>"
                + "     <name>testContext</name>"
                + "		<type>ieee80211</type>"
                + "     </interface>"
                + "    </interfaces>"
                + "		<tconts-config xmlns=\"https://interface-ref\"/>"
                + "   </schemaMountPoint>" 
                + "  </xml-subtree>"
                + " </validation>";
        editConfig(requestXml);
    }

    @Test
    public void testRpcOnMountedSchema() throws Exception {

        RpcRequestHandlerRegistry registry = new RpcRequestHandlerRegistryImpl();
        registry.registerMultiRpcRequestHandler(new DeviceMultiRpcRequestHandler());
        m_server.setRpcRequestHandlerRegistry(registry);


        // adding some data to make schema mount point initialized.
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "  <xml-subtree>"
                + "  <plugType>PLUG-1.0</plugType>" 
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface>"
                + "     <name>WLAN-1</name>"
                + "     <type>ieee80211</type>"
                + "     </interface>"
                + "    </interfaces>" 
                + "   </schemaMountPoint>" 
                + "  </xml-subtree>"
                + " </validation>";
        editConfig(requestXml);

        requestXml = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> \n"
                + "   <action xmlns=\"urn:ietf:params:xml:ns:yang:1\"> \n"
                + "       <validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "           <xml-subtree>"
                + "               <plugType>PLUG-1.0</plugType>" 
                + "               <schemaMountPoint>"
                + "                   <smt:select-station-ssid xmlns:smt=\"schema-mount-test\"> \n"
                + "                       <smt:wigig-interface>WLAN-1</smt:wigig-interface> \n"
                + "                       <smt:ssid>NSB_2_2</smt:ssid> \n"
                + "                   </smt:select-station-ssid> \n" 
                + "               </schemaMountPoint>"
                + "           </xml-subtree>" 
                + "       </validation>" 
                + "   </action> \n" 
                + "</rpc>";
        ActionRequest request = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(requestXml));
        NetConfResponse response = new ActionResponse().setMessageId("1");
        m_server.onAction(m_clientInfo, request, (ActionResponse) response);
        assertTrue(response.isOk());
    }

    @Test
    public void testMultipleRpcOnMountedSchema() throws Exception {

        RpcRequestHandlerRegistry registry = new RpcRequestHandlerRegistryImpl();
        registry.registerMultiRpcRequestHandler(new DeviceMultiRpcRequestHandler());
        m_server.setRpcRequestHandlerRegistry(registry);


        // adding some data to make schema mount point initialized.
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + " <validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "  <xml-subtree>"
                + "  <plugType>PLUG-1.0</plugType>" 
                + "   <schemaMountPoint>"
                + "    <interfaces xmlns=\"test-interfaces\">"
                + "     <interface>"
                + "     <name>WLAN-1</name>"
                + "     <type>ieee80211</type>"
                + "     </interface>"
                + "    </interfaces>" 
                + "   </schemaMountPoint>" 
                + "  </xml-subtree>"
                + " </validation>";
        editConfig(requestXml);

        requestXml = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"> \n"
                + "   <action xmlns=\"urn:ietf:params:xml:ns:yang:1\"> \n"
                + "       <validation xmlns=\"urn:org:bbf2:pma:validation\">" 
                + "           <xml-subtree>"
                + "               <plugType>PLUG-1.0</plugType>" 
                + "               <schemaMountPoint>"
                + "                   <smt:select-station-ssid xmlns:smt=\"schema-mount-test\"> \n"
                + "                       <smt:wigig-interface>WLAN-1</smt:wigig-interface> \n"
                + "                       <smt:ssid>NSB_2_2</smt:ssid> \n"
                + "                   </smt:select-station-ssid> \n" 
                + "                   <smt:other-select-station-ssid xmlns:smt=\"schema-mount-test\"> \n"
                + "                       <smt:wigig-interface>WLAN-1</smt:wigig-interface> \n"
                + "                       <smt:ssid>NSB_2_2</smt:ssid> \n"
                + "                   </smt:other-select-station-ssid> \n" 
                + "               </schemaMountPoint>"
                + "           </xml-subtree>" 
                + "       </validation>" 
                + "   </action> \n" 
                + "</rpc>";
        ActionRequest request = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(requestXml));
        NetConfResponse response = new ActionResponse().setMessageId("1");
        m_server.onAction(m_clientInfo, request, (ActionResponse) response);
        NetconfRpcError error = response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("Multiple action element exists within RPC", error.getErrorMessage());
    }

    @Test
    public void testMustConstraintBooleanType_AbsoluteXPathWithPrefix() throws Exception {

        // create channel-group and channel-partition 
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_group</if:name>" +
                        "         <if:enabled>true</if:enabled>" +   					
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-group>"+
                        "		  	<if:system-id>10</if:system-id>"+
                        "		  </if:channel-group>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        "		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        " 			<if:channel-partition-index>5</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // create channel-pair
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        " 		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "		  	<if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "			<smt:channel-pair-type xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //verify GET request
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                        "      <if:interface>"+
                        "       <if:channel-group>"+
                        "         <if:system-id>10</if:system-id>"+
                        "       </if:channel-group>"+
                        "      <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "      <if:name>intf_group</if:name>"+
                        "      <if:type>if:vlan-sub-interface</if:type>"+
                        "	  </if:interface>"+
                        "     <if:interface>"+
                        "      <if:channel-partition>"+
                        "       <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <if:channel-partition-index>5</if:channel-partition-index>"+
                        "       </if:channel-partition>"+
                        "       <if:enabled>true</if:enabled>"+
                        "         <if:mybits>fourthBit</if:mybits>" +
                        "         <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"+
                        "         <if:mybitsoverridden>fourthBit</if:mybitsoverridden>" +
                        "       <if:name>intf_partition</if:name>"+
                        "       <if:type>if:vlan-sub-interface</if:type>"+
                        "      </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "        <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <smt:channel-pair-type xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type>"+
                        "        <if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>" + 
                        "          <if:mybits>fourthBit</if:mybits>\n"+ 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+ 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "   </if:interfaces>"+
                        "  </schemaMountPoint>"+
                        " </validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // remove channel-partition-index leaf and Impact validation  should be failed
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "		  <if:channel-partition>"+
                        " 			<if:channel-partition-index xc:operation=\"remove\">5</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); 

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='intf_pair']/if:channel-pair/smt:channel-pair-type", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: boolean(/test1:interfaces/test1:interface[test1:name=current()/../test1:channel-partition-ref]/test1:channel-partition/test1:channel-partition-index)", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());	
    }

    @Test
    public void testMustConstraintBooleanType_AbsoluteXPathWithMultipleCondition() throws Exception {

        // create channel-group and channel-partition 
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_group</if:name>" +    					
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-group>"+
                        "		  	<if:system-id>10</if:system-id>"+
                        "		  </if:channel-group>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        "		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        " 			<if:channel-partition-index>5</if:channel-partition-index>"+
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // create channel-pair
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        " 		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "		  	<if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "			<smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type1>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass


        //verify GET request
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                        "      <if:interface>"+
                        "       <if:channel-group>"+
                        "         <if:system-id>10</if:system-id>"+
                        "       </if:channel-group>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+ 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+ 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "      <if:name>intf_group</if:name>"+
                        "      <if:type>if:vlan-sub-interface</if:type>"+
                        "	  </if:interface>"+
                        "     <if:interface>"+
                        "      <if:channel-partition>"+
                        "       <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <if:channel-partition-index>5</if:channel-partition-index>"+
                        "       </if:channel-partition>"+
                        "       <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+ 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+ 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "       <if:name>intf_partition</if:name>"+
                        "       <if:type>if:vlan-sub-interface</if:type>"+
                        "      </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "        <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type1>"+
                        "        <if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+ 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+ 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "   </if:interfaces>"+
                        "  </schemaMountPoint>"+
                        " </validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);


        // create channel-pair alone
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair1</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        "			<smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">ngpon</smt:channel-pair-type1>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass


        //verify GET request
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                        "      <if:interface>"+
                        "       <if:channel-group>"+
                        "         <if:system-id>10</if:system-id>"+
                        "       </if:channel-group>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+ 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+ 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "      <if:name>intf_group</if:name>"+
                        "      <if:type>if:vlan-sub-interface</if:type>"+
                        "	  </if:interface>"+
                        "     <if:interface>"+
                        "      <if:channel-partition>"+
                        "       <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <if:channel-partition-index>5</if:channel-partition-index>"+
                        "       </if:channel-partition>"+
                        "       <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "       <if:name>intf_partition</if:name>"+
                        "       <if:type>if:vlan-sub-interface</if:type>"+
                        "      </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "        <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type1>"+
                        "        <if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "        <smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">ngpon</smt:channel-pair-type1>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair1</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "   </if:interfaces>"+
                        "  </schemaMountPoint>"+
                        " </validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);
    }


    @Test
    public void testMustConstraintBooleanType_AbsoluteXPathWithMultipleCondition_Fail() throws Exception {

        // create channel-group and channel-partition 
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_group</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-group>"+
                        "		  </if:channel-group>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        "		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        " 			<if:channel-partition-index>5</if:channel-partition-index>"+
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // create channel-pair and must constraint should be failed
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        " 		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "		  	<if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "			<smt:channel-pair-type1 xmlns:smt=\"schema-mount-test\">type</smt:channel-pair-type1>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); 

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='intf_pair']/if:channel-pair/smt:channel-pair-type1", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: (boolean(/test1:interfaces/test1:interface[test1:name=current()/../test1:channel-partition-ref]/test1:channel-partition/test1:channel-partition-index) and boolean(/test1:interfaces/test1:interface[test1:name=current()/../test1:channel-group-ref]/test1:channel-group/test1:system-id)) or current() = 'ngpon'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testMustConstraintBooleanType_RelativeXPathWithPrefix() throws Exception {

        // create channel-group and channel-partition 
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_group</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-group>"+
                        "		  	<if:system-id>10</if:system-id>"+
                        "		  </if:channel-group>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        "		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        " 			<if:channel-partition-index>5</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // create channel-pair
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        " 		  	<if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "		  	<if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "			<smt:boolean-with-relative-path xmlns:smt=\"schema-mount-test\">type</smt:boolean-with-relative-path>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //verify GET request
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                        "      <if:interface>"+
                        "       <if:channel-group>"+
                        "         <if:system-id>10</if:system-id>"+
                        "       </if:channel-group>"+
                        "      <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "      <if:name>intf_group</if:name>"+
                        "      <if:type>if:vlan-sub-interface</if:type>"+
                        "	  </if:interface>"+
                        "     <if:interface>"+
                        "      <if:channel-partition>"+
                        "       <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <if:channel-partition-index>5</if:channel-partition-index>"+
                        "       </if:channel-partition>"+
                        "       <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "       <if:name>intf_partition</if:name>"+
                        "       <if:type>if:vlan-sub-interface</if:type>"+
                        "      </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "        <if:channel-group-ref>intf_group</if:channel-group-ref>"+
                        "        <smt:boolean-with-relative-path xmlns:smt=\"schema-mount-test\">type</smt:boolean-with-relative-path>"+
                        "        <if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "   </if:interfaces>"+
                        "  </schemaMountPoint>"+
                        " </validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // remove channel-partition-index leaf and Impact validation  should be failed
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "		  <if:channel-partition>"+
                        " 			<if:channel-partition-index xc:operation=\"remove\">5</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); 
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='intf_pair']/if:channel-pair/smt:boolean-with-relative-path", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: boolean(../../../test1:interface[test1:name=current()/../test1:channel-partition-ref]/test1:channel-partition/test1:channel-partition-index)", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());	
    }

    @Test
    public void testMustConstraintStringType_AbsoluteXPathWithPrefix() throws Exception {

        // create channel-pair and channel-partition 
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        " 			<if:channel-partition-index>5</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        "		  	<if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "			<smt:string-funtion-leaf xmlns:smt=\"schema-mount-test\">5</smt:string-funtion-leaf>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        //verify GET request
        String response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "    <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "     <if:interfaces xmlns:if=\"test-interfaces\">"+
                        "     <if:interface>"+
                        "      <if:channel-partition>"+
                        "        <if:channel-partition-index>5</if:channel-partition-index>"+
                        "       </if:channel-partition>"+
                        "       <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "       <if:name>intf_partition</if:name>"+
                        "       <if:type>if:vlan-sub-interface</if:type>"+
                        "      </if:interface>"+
                        "      <if:interface>"+
                        "       <if:channel-pair>"+
                        "		<smt:string-funtion-leaf xmlns:smt=\"schema-mount-test\">5</smt:string-funtion-leaf>"+
                        "        <if:channel-partition-ref>intf_partition</if:channel-partition-ref>"+
                        "       </if:channel-pair>"+
                        "     <if:enabled>true</if:enabled>"+
                        "          <if:mybits>fourthBit</if:mybits>\n"+
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n"+
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "     <if:name>intf_pair</if:name>"+
                        "     <if:type>if:vlan-sub-interface</if:type>"+
                        "    </if:interface>"+
                        "   </if:interfaces>"+
                        "  </schemaMountPoint>"+
                        " </validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        //Failure case - must constraint  should fail
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf_partition1</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-partition>"+
                        " 			<if:channel-partition-index>10</if:channel-partition-index>"+		
                        "		  </if:channel-partition>"+
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf_pair1</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "		  <if:channel-pair>"+
                        "		  	<if:channel-partition-ref>intf_partition1</if:channel-partition-ref>"+
                        "			<smt:string-funtion-leaf xmlns:smt=\"schema-mount-test\">5</smt:string-funtion-leaf>"+
                        "		  </if:channel-pair>"+
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); 
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/if:interfaces/if:interface[if:name='intf_pair1']/if:channel-pair/smt:string-funtion-leaf", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: string(/test1:interfaces/test1:interface[test1:name=current()/../test1:channel-partition-ref]/test1:channel-partition/test1:channel-partition-index) = current()", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testLeafRefValidationWithDifferentRootNodes_RemoveLeafRefNode() throws Exception {

        // create interfaces and sub-interfaces
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface> " +
                        "         <if:name>intf1</if:name>" +
                        "          <if:mybits>fourthBit</if:mybits>\n" + 
                        "           <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "           <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:multicast-interface-to-host>" +
                        "          <smt:name>interfaceHost1</smt:name>" +
                        "          <smt:sub-interface>intf1</smt:sub-interface>" +
                        "        </smt:multicast-interface-to-host>" +
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
                        "<if:interfaces xmlns:if=\"test-interfaces\">"+
                        "<if:interface>"+
                        "<if:enabled>true</if:enabled>"+
                        "<if:mybits>fourthBit</if:mybits>\n" + 
                        "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "<if:name>intf1</if:name>"+
                        "<if:type>if:vlan-sub-interface</if:type>"+
                        "</if:interface>"+
                        "</if:interfaces>"+
                        "<smt:multicast xmlns:smt=\"schema-mount-test\">"+
                        "<smt:mgmd>"+
                        "<smt:multicast-vpn>"+
                        "<smt:multicast-interface-to-host>"+
                        "<smt:name>interfaceHost1</smt:name>"+
                        "<smt:sub-interface>intf1</smt:sub-interface>"+
                        "</smt:multicast-interface-to-host>"+
                        "<smt:name>multicast</smt:name>"+
                        "</smt:multicast-vpn>"+
                        "</smt:mgmd>"+
                        "</smt:multicast>"+
                        "</schemaMountPoint>"+
                        "</validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // Remove interfaces and it's associated sub-interface leaf
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface xc:operation=\"remove\"> " +
                        "         <if:name>intf1</if:name>" +
                        "      </if:interface>" +
                        "      <if:interface> " +
                        "         <if:name>intf2</if:name>" +
                        "         <if:enabled>true</if:enabled>" +
                        "         <if:type>if:vlan-sub-interface</if:type>" +
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "    <smt:multicast xmlns:smt=\"schema-mount-test\">" +
                        "     <smt:mgmd>" +
                        "      <smt:multicast-vpn>" +
                        "        <smt:name>multicast</smt:name>" +
                        "        <smt:multicast-interface-to-host xc:operation=\"merge\">" +
                        "          <smt:name>interfaceHost2</smt:name>" +
                        "          <smt:sub-interface>intf2</smt:sub-interface>" +
                        "        </smt:multicast-interface-to-host>" +
                        "        <smt:multicast-interface-to-host xc:operation=\"remove\">" +
                        "          <smt:name>interfaceHost1</smt:name>" +
                        "        </smt:multicast-interface-to-host>" +
                        "      </smt:multicast-vpn>" +
                        "     </smt:mgmd>" +
                        "    </smt:multicast>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request); // Should pass

        // Verify GET after deletion of intf1
        response = 
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "<validation:xml-subtree>"+
                        "<validation:plugType>PLUG-1.0</validation:plugType>"+
                        "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "<if:interfaces xmlns:if=\"test-interfaces\">"+
                        "<if:interface>"+
                        "<if:enabled>true</if:enabled>"+
                        "<if:mybits>fourthBit</if:mybits>\n" + 
                        "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" + 
                        "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                        "<if:name>intf2</if:name>"+
                        "<if:type>if:vlan-sub-interface</if:type>"+
                        "</if:interface>"+
                        "</if:interfaces>"+
                        "<smt:multicast xmlns:smt=\"schema-mount-test\">"+
                        "<smt:mgmd>"+
                        "<smt:multicast-vpn>"+
                        "<smt:multicast-interface-to-host>"+
                        "<smt:name>interfaceHost2</smt:name>"+
                        "<smt:sub-interface>intf2</smt:sub-interface>"+
                        "</smt:multicast-interface-to-host>"+
                        "<smt:name>multicast</smt:name>"+
                        "</smt:multicast-vpn>"+
                        "</smt:mgmd>"+
                        "</smt:multicast>"+
                        "</schemaMountPoint>"+
                        "</validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";
        verifyGet(response);

        // remove only interface without it's leafref value
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "     <if:interfaces xmlns:if=\"test-interfaces\">" +
                        "      <if:interface xc:operation=\"remove\"> " +
                        "         <if:name>intf2</if:name>" +
                        "      </if:interface>" +
                        "     </if:interfaces>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:multicast/smt:mgmd/smt:multicast-vpn[smt:name='multicast']/smt:multicast-interface-to-host[smt:name='interfaceHost2']/smt:sub-interface", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, 'intf2' must exist", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("instance-required", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfResponse.getErrors().get(0).getErrorTag());
    }

    @Test
    public void testLeafRefWithNestedList_RemoveLeafRefNode() throws Exception {

        // Create queue list
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "		<configure>"+
                "			<qos>"+
                "				<sap-ingress>"+
                "					<sap-ingress-policy-name>my-policy</sap-ingress-policy-name>"+
                "					<queue>"+
                "						<queue-id>1</queue-id>"+
                "					</queue>"+
                "				</sap-ingress>"+
                "			</qos>"+
                "			<service>"+
                "				<ies>"+
                "					<service-name>ies-999</service-name>"+
                "					<interface>"+
                "						<interface-name>telnet</interface-name>"+
                "						<sap>"+
                "							<sap-id>99</sap-id>"+
                "							<ingress>"+
                "								<qos>"+
                "									<sap-ingress>"+
                "										<policy-name>my-policy</policy-name>"+
                "										<overrides>"+
                "											<queue>"+
                "												<queue-id>1</queue-id>"+
                "											</queue>"+
                "										</overrides>"+
                "									</sap-ingress>"+
                "								</qos>"+
                "							</ingress>"+
                "						</sap>"+
                "					</interface>"+
                "				</ies>"+
                "			</service>"+
                "		</configure>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        // create another queue list
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "		<configure>"+
                "			<qos>"+
                "				<sap-ingress>"+
                "					<sap-ingress-policy-name>my-policy1</sap-ingress-policy-name>"+
                "					<queue>"+
                "						<queue-id>1</queue-id>"+
                "					</queue>"+
                "					<queue xc:operation=\"create\">"+
                "						<queue-id>3</queue-id>"+
                "					</queue>"+
                "				</sap-ingress>"+
                "			</qos>"+
                "			<service>"+
                "				<ies>"+
                "					<service-name>ies-999</service-name>"+
                "					<interface>"+
                "						<interface-name>telnet</interface-name>"+
                "						<sap>"+
                "							<sap-id>99</sap-id>"+
                "							<ingress>"+
                "								<qos>"+
                "									<sap-ingress>"+
                "										<policy-name>my-policy1</policy-name>"+
                "										<overrides>"+
                "											<queue>"+
                "												<queue-id>1</queue-id>"+
                "											</queue>"+
                "											<queue xc:operation=\"create\">"+
                "												<queue-id>3</queue-id>"+
                "											</queue>"+
                "										</overrides>"+
                "									</sap-ingress>"+
                "								</qos>"+
                "							</ingress>"+
                "						</sap>"+
                "					</interface>"+
                "				</ies>"+
                "			</service>"+
                "		</configure>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        // Verify Get Response
        String response = 

                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"+
                        "<data>"+
                        " <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"+
                        "  <validation:xml-subtree>"+
                        "   <validation:plugType>PLUG-1.0</validation:plugType>"+
                        "   <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">"+
                        "    <smt:schemaMount xmlns:smt=\"schema-mount-test\">"+
                        "     <smt:configure>"+
                        "      <smt:qos>"+
                        "       <smt:sap-ingress>"+
                        "        <smt:queue>"+
                        "         <smt:queue-id>1</smt:queue-id>"+
                        "        </smt:queue>"+
                        "        <smt:sap-ingress-policy-name>my-policy</smt:sap-ingress-policy-name>"+
                        "      </smt:sap-ingress>"+
                        "      <smt:sap-ingress>"+
                        "       <smt:queue>"+
                        "         <smt:queue-id>1</smt:queue-id>"+
                        "       </smt:queue>"+
                        "      <smt:queue>"+
                        "        <smt:queue-id>3</smt:queue-id>"+
                        "      </smt:queue>"+
                        "    <smt:sap-ingress-policy-name>my-policy1</smt:sap-ingress-policy-name>"+
                        "   </smt:sap-ingress>"+
                        "  </smt:qos>"+
                        "  <smt:service>"+
                        "   <smt:ies>"+
                        "    <smt:interface>"+
                        "     <smt:interface-name>telnet</smt:interface-name>"+
                        "     <smt:sap>"+
                        "      <smt:ingress>"+
                        "       <smt:qos>"+
                        "        <smt:sap-ingress>"+
                        "         <smt:overrides>"+
                        "          <smt:queue>"+
                        "            <smt:queue-id>1</smt:queue-id>"+
                        "          </smt:queue>"+
                        "          <smt:queue>"+
                        "            <smt:queue-id>3</smt:queue-id>"+
                        "          </smt:queue>"+
                        "        </smt:overrides>"+
                        "        <smt:policy-name>my-policy1</smt:policy-name>"+
                        "      </smt:sap-ingress>"+
                        "     </smt:qos>"+
                        "    </smt:ingress>"+
                        "   <smt:sap-id>99</smt:sap-id>"+
                        "  </smt:sap>"+
                        "  </smt:interface>"+
                        "  <smt:service-name>ies-999</smt:service-name>"+
                        " </smt:ies>"+
                        "</smt:service>"+
                        "</smt:configure>"+
                        "</smt:schemaMount>"+
                        "</schemaMountPoint>"+
                        "</validation:xml-subtree>"+
                        "</validation:validation>"+
                        "</data>"+
                        "</rpc-reply>";

        verifyGet(response);

        // Remove valid leaf-ref value
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "		<configure>"+
                "			<qos>"+
                "				<sap-ingress>"+
                "					<sap-ingress-policy-name>my-policy1</sap-ingress-policy-name>"+
                "					<queue xc:operation=\"delete\">"+
                "						<queue-id>3</queue-id>"+
                "					</queue>"+
                "				</sap-ingress>"+
                "			</qos>"+
                "			<service>"+
                "				<ies>"+
                "					<service-name>ies-999</service-name>"+
                "					<interface>"+
                "						<interface-name>telnet</interface-name>"+
                "						<sap>"+
                "							<sap-id>99</sap-id>"+
                "							<ingress>"+
                "								<qos>"+
                "									<sap-ingress>"+
                "										<policy-name>my-policy1</policy-name>"+
                "										<overrides>"+
                "											<queue xc:operation=\"delete\">"+
                "												<queue-id>3</queue-id>"+
                "											</queue>"+
                "										</overrides>"+
                "									</sap-ingress>"+
                "								</qos>"+
                "							</ingress>"+
                "						</sap>"+
                "					</interface>"+
                "				</ies>"+
                "			</service>"+
                "		</configure>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";
        editConfig(request);

        // Remove leaf-ref value alone
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +                   
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" + 
                "  <xml-subtree>" + 
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "		<configure>"+
                "			<qos>"+
                "				<sap-ingress>"+
                "					<sap-ingress-policy-name>my-policy1</sap-ingress-policy-name>"+
                "					<queue xc:operation=\"delete\">"+
                "						<queue-id>1</queue-id>"+
                "					</queue>"+
                "				</sap-ingress>"+
                "			</qos>"+
                "		</configure>"+
                "    </schemaMount>" +
                "   </schemaMountPoint>" + 
                "  </xml-subtree>" + 
                " </validation>";

        NetConfResponse netconfResponse = editConfigAsFalse(request); // Should Fail

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:configure/smt:service/smt:ies[smt:service-name='ies-999']/smt:interface[smt:interface-name='telnet']/smt:sap[smt:sap-id='99']/smt:ingress/smt:qos/smt:sap-ingress/smt:overrides/smt:queue[smt:queue-id='1']/smt:queue-id", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Dependency violated, '1' must exist", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals("instance-required", netconfResponse.getErrors().get(0).getErrorAppTag());
        assertEquals(NetconfRpcErrorTag.DATA_MISSING, netconfResponse.getErrors().get(0).getErrorTag());

    }

    @Test
    public void testHintsOnNodeSkipValidation() throws Exception {

        // fails as there is no hint on the leafWithHint2 refers to leafWithHint1
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint1>bbf</leafWithHint1>" +
                        "       <leafWithHint2>bbf</leafWithHint2>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); 
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:hintList[smt:keyLeaf='test1']/smt:leafWithHint2", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: count(/smt:schemamount/smt:hintList/smt:leafWithHint1 = 'hello') > 1", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());

        // passes as there is SKIP_VALIDATION hint on the leafWithHint2 refers to leafWithHint1, so neither regular or impact validation is done
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint1>bbf</leafWithHint1>" +
                        "       <leafWithHint3>bbf</leafWithHint3>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        // passes as there is SKIP_VALIDATION hint on the leafWithHint2 refers to leafWithHint1, so neither regular or impact validation is done
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint1>bbf1</leafWithHint1>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
    }

    @Test
    public void testHintsOnNodeSkipImpactValidation() throws Exception {

        // fails as there is SKIP_IMPACT_VALIDATION hint on the leafWithHint5 refers to leafWithHint4, so still regular validation applies but not the impact validation
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint4>bbf</leafWithHint4>" +
                        "       <leafWithHint5>bbf</leafWithHint5>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        NetConfResponse netconfResponse = editConfigAsFalse(request); 
        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:schemaMount/smt:hintList[smt:keyLeaf='test1']/smt:leafWithHint5", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate when constraints: ../leafWithHint4 = 'hello'", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.UNKNOWN_ELEMENT, netconfResponse.getErrors().get(0).getErrorTag());

        // passes regular validation
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint4>hello</leafWithHint4>" + 
                        "       <leafWithHint5>bbf</leafWithHint5>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);

        // passes as there is SKIP_IMPACT_VALIDATION hint on the leafWithHint5 refers to leafWithHint4, so passes regular validation but impact validation fails here
        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint4>bbf1</leafWithHint4>" +
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
    }

    @Test
    public void testHintsOnNodeSkipImpactOnCreateValidation() throws Exception {

        // succeeds as there is SKIP_IMPACT_ON_CREATE hint on the leafWithHint6 refers to leafWithHint7, so still regular validation applies but not the impact validation during create
        String request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test1</keyLeaf>" + 
                        "       <leafWithHint7>carrot</leafWithHint7>" + 
                        "       <leafWithHint6>hello</leafWithHint6>" + 
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;

        editConfig(request);

        request = 
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                        " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                        "  <xml-subtree>" +
                        "   <plugType>PLUG-1.0</plugType>" +
                        "   <schemaMountPoint>" +
                        "    <schemaMount xmlns=\"schema-mount-test\">" + 
                        "     <hintList>" + 
                        "       <keyLeaf>test2</keyLeaf>" + 
                        "       <leafWithHint7>carrot</leafWithHint7>" + 
                        "	  </hintList>"+
                        "    </schemaMount>" +
                        "   </schemaMountPoint>" +
                        "  </xml-subtree>" +
                        " </validation>"
                        ;
        editConfig(request);
    }

    @Test
    public void testCurrentInRootList() throws Exception {

        // hardware-state is up --> created augment node 'component'
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardwaresList xmlns=\"schema-mount-test\">" +
                "       <hardware>" +
                "           <name>BP</name>" +
                "       </hardware>" +
                "    </hardwaresList>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);
    }

    @Test
    public void testCurrentInRootContainer() throws Exception {

        // hardware-state is up --> created augment node 'component'
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardwares xmlns=\"schema-mount-test\">" +
                "       <hardware>" +
                "           <name>BP</name>" +
                "       </hardware>" +
                "    </hardwares>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardwares xmlns=\"schema-mount-test\">" +
                "       <hardware>" +
                "           <name>dummy</name>" +
                "       </hardware>" +
                "    </hardwares>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);
        String expectedOutput = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:hardwares xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:hardware>\n" + 
                "<smt:name>BP</smt:name>\n" + 
                "</smt:hardware>\n" + 
                "<smt:hardware>\n" + 
                "<smt:name>dummy</smt:name>\n" + 
                "</smt:hardware>\n" + 
                "</smt:hardwares>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardwares xmlns=\"schema-mount-test\">" +
                "       <hardware xc:operation=\"remove\">" +
                "           <name>dummy</name>" +
                "       </hardware>" +
                "    </hardwares>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, true);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardwares xmlns=\"schema-mount-test\">" +
                "       <hardware xc:operation=\"remove\">" +
                "           <name>BP</name>" +
                "       </hardware>" +
                "    </hardwares>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(m_server, m_clientInfo, request, false);
    }

    @Test
    public void testIdRefWithDifferentPrefixFromDeviation() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf6 xmlns:aa=\"test-interfaces\">aa:english</leaf6>" +
                "      <leaf8>leaf8</leaf8>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf6 xmlns:if=\"test-interfaces\">if:english</smt:leaf6>\n" + 
                "<smt:leaf8>leaf8</smt:leaf8>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf6 xmlns:aa=\"test-interfaces\">aa:french</leaf6>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf6 should be of type english");
    }

    @Test
    public void testIdRefWithDifferentPrefixInImportedModule() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf5 xmlns:aa=\"test-interfaces\">aa:english</leaf5>" +
                "      <leaf7>leaf7</leaf7>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf5 xmlns:if=\"test-interfaces\">if:english</smt:leaf5>\n" + 
                "<smt:leaf7>leaf7</smt:leaf7>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf5 xmlns:aa=\"test-interfaces\">aa:french</leaf5>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf5 should be of type english");
    }

    @Test
    public void testIdRefWithEqualityOperationDifferentPrefixInImportedModule() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf9 xmlns:aa=\"test-interfaces\">aa:english</leaf9>" +
                "      <leaf10>leaf10</leaf10>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf9 xmlns:if=\"test-interfaces\">if:english</smt:leaf9>\n" + 
                "<smt:leaf10>leaf10</smt:leaf10>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf9 xmlns:aa=\"test-interfaces\">aa:french</leaf9>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf9 should be of type english");
    }

    @Test
    public void testIdRefWithEqualityOperationDifferentPrefixInDeviationModule() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf11 xmlns:aa=\"test-interfaces\">aa:english</leaf11>" +
                "      <leaf12>leaf12</leaf12>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf11 xmlns:if=\"test-interfaces\">if:english</smt:leaf11>\n" + 
                "<smt:leaf12>leaf12</smt:leaf12>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf11 xmlns:aa=\"test-interfaces\">aa:french</leaf11>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf11 should be of type english");
    }

    @Test
    public void testIdRefWithEqualityOperationDifferentPrefixWithCurrentOnly() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf13 xmlns:aa=\"test-interfaces\">aa:english</leaf13>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf13 xmlns:if=\"test-interfaces\">if:english</smt:leaf13>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf13 xmlns:aa=\"test-interfaces\">aa:french</leaf13>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf13 should be of type english");
    }

    @Test
    public void testIdRefWithEqualityOperationDifferentPrefixContainsCurrent() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf11 xmlns:aa=\"test-interfaces\">aa:english</leaf11>" +
                "      <leaf14>leaf14</leaf14>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf11 xmlns:if=\"test-interfaces\">if:english</smt:leaf11>\n" + 
                "<smt:leaf14>leaf14</smt:leaf14>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <idRefContainerForDeviations>" +
                "      <leaf11 xmlns:aa=\"test-interfaces\">aa:french</leaf11>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "leaf11 should be of type english");
    }

    @Test
    public void testIdRefWithEqualityOperationDifferentPrefixNegativeCase() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "      <leaf11>english</leaf11>" +
                "      <leaf14>leaf14</leaf14>" +  
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        String errorMsg = "Value \"english\" is not a valid identityref value.";
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals(errorMsg, error.getErrorMessage());
        assertEquals(NetconfRpcErrorTag.INVALID_VALUE, error.getErrorTag());       
    }

    @Test
    public void testIdRefWithBooleanFunctionWithAbsolutePath() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP1</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>Board</parent>" +
                "      <model-name>3FE62600AA</model-name>" +
                "     </component>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:french</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:english</smt:class>\n" + 
                "<smt:model-name>3FE62600AA</smt:model-name>\n" + 
                "<smt:name>SFP1</smt:name>\n" + 
                "<smt:parent>Board</smt:parent>\n" + 
                "</smt:component>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:french</smt:class>\n" + 
                "<smt:name>SFP-Link</smt:name>\n" + 
                "<smt:parent>SFP1</smt:parent>\n" + 
                "</smt:component>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "component mismatch error");
    }

    @Test
    public void testIdRefWithBooleanFunctionWithRelativePath() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP1</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>Board</parent>" +
                "      <model-name1>3FE62600AA</model-name1>" +
                "     </component>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:french</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:english</smt:class>\n" + 
                "<smt:model-name1>3FE62600AA</smt:model-name1>\n" + 
                "<smt:name>SFP1</smt:name>\n" + 
                "<smt:parent>Board</smt:parent>\n" + 
                "</smt:component>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:french</smt:class>\n" + 
                "<smt:name>SFP-Link</smt:name>\n" + 
                "<smt:parent>SFP1</smt:parent>\n" + 
                "</smt:component>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "component mismatch error");
    }

    @Test
    public void testCountFunctionWithCurrentNegativeCase() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <listWithCount>" +
                "      <name>first</name>" +
                "      <non-key-leaf>NOK</non-key-leaf>" +
                "    </listWithCount>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate must constraints: (count(current()[non-key-leaf = 'ok']) = 1)", error.getErrorMessage());
    }

    @Test
    public void testCountFunctionWithCurrentPositiveCase() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <listWithCount>" +
                "      <name>first</name>" +
                "      <non-key-leaf>NOK</non-key-leaf>" +
                "    </listWithCount>" +
                "    <listWithCount>" +
                "      <name>second</name>" +
                "      <non-key-leaf>ok</non-key-leaf>" +
                "    </listWithCount>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <listWithCount xc:operation=\"remove\">" +
                "      <name>second</name>" +
                "      <non-key-leaf>ok</non-key-leaf>" +
                "    </listWithCount>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate must constraints: (count(current()[non-key-leaf = 'ok']) = 1)", error.getErrorMessage());
    }

    @Test
    public void testIdRefWithCountFunctionWithRelativePath() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP1</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:french</class>" +
                "      <parent>Board</parent>" +
                "      <countleaf>enabled</countleaf>" +
                "     </component>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:french</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:french</smt:class>\n" + 
                "<smt:countleaf>enabled</smt:countleaf>\n" + 
                "<smt:name>SFP1</smt:name>\n" + 
                "<smt:parent>Board</smt:parent>\n" + 
                "</smt:component>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:french</smt:class>\n" + 
                "<smt:name>SFP-Link</smt:name>\n" + 
                "<smt:parent>SFP1</smt:parent>\n" + 
                "</smt:component>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";
        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "class of type french >1");
    }

    @Test
    public void testIdRefWithBooleanFunction() throws Exception {
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP1</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>Board</parent>" +
                "      <model-name2>3FE62600AA</model-name2>" +
                "     </component>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:french</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, request, true);

        String expectedOutput = " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" + 
                "<data>\n" + 
                "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" + 
                "<validation:xml-subtree>\n" + 
                "<validation:plugType>PLUG-1.0</validation:plugType>\n" + 
                "<schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" + 
                "<smt:schemaMount xmlns:smt=\"schema-mount-test\">\n" + 
                "<smt:idRefContainerForDeviations>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:english</smt:class>\n" + 
                "<smt:model-name2>3FE62600AA</smt:model-name2>\n" + 
                "<smt:name>SFP1</smt:name>\n" + 
                "<smt:parent>Board</smt:parent>\n" + 
                "</smt:component>\n" + 
                "<smt:component>\n" + 
                "<smt:class xmlns:if=\"test-interfaces\">if:french</smt:class>\n" + 
                "<smt:name>SFP-Link</smt:name>\n" + 
                "<smt:parent>SFP1</smt:parent>\n" + 
                "</smt:component>\n" + 
                "</smt:idRefContainerForDeviations>\n" + 
                "<smt:leaf4>leaf4</smt:leaf4>\n" + 
                "</smt:schemaMount>\n" + 
                "</schemaMountPoint>\n" + 
                "</validation:xml-subtree>\n" + 
                "</validation:validation>\n" + 
                "</data>\n" + 
                "</rpc-reply>";

        verifyGet(expectedOutput);

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "  <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <schemaMount xmlns=\"schema-mount-test\">" +
                "    <leaf4>leaf4</leaf4>" +
                "    <idRefContainerForDeviations>" +
                "     <component>" +
                "      <name>SFP-Link</name>" +
                "      <class xmlns:aa=\"test-interfaces\">aa:english</class>" +
                "      <parent>SFP1</parent>" +
                "     </component>" +
                "    </idRefContainerForDeviations>" +
                "    </schemaMount>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        response = editConfig(m_server, m_clientInfo, request, false);
        checkErrors(response, "must-violation", "component mismatch error with model-name2");
    }
    
    private class DeviceMultiRpcRequestHandler implements MultiRpcRequestHandler {

        public DeviceMultiRpcRequestHandler() {
        }

        @Override
        public String checkForRpcSupport(RpcName rpcName) {
            return "http://www.test-company.com/solutions/anv/device/";
        }

        @Override
        public void checkRequiredPermissions(NetconfClientInfo clientInfo, String operation) throws AccessDeniedException {

        }

        public boolean isSchemaMountedRpcRequestHandler(){
            return true;
        }

        @Override
        public void validate(RpcPayloadConstraintParser rpcConstraintParser, final NetconfMessage rpc) throws RpcValidationException {
            try {
                TimingLogger.withStartAndFinish(() -> {
                    if (rpc.getType().isRequest()){
                        NetconfRpcRequest rpcReq = (NetconfRpcRequest)rpc;
                        Element rpcElement = rpcReq.getRpcInput();
                        MountProviderInfo providerInfo = SchemaRegistryUtil.getMountProviderInfo(rpcElement , m_schemaRegistry);
                        if ( providerInfo != null){
                            Element rpcElementWithoutDeviceData = DocumentUtils.getChildElement((Element) providerInfo.getMountedXmlNodeFromRequest());
                            rpcReq.setRpcInput(rpcElementWithoutDeviceData);
                        }
                        rpcConstraintParser.validate(rpcReq, RequestType.RPC);
                        rpcReq.setRpcInput(rpcElement);
                    }else if (rpc.getType().isResponse()){
                        rpcConstraintParser.validate((NetconfRpcResponse)rpc, RequestType.RPC);
                    }
                });

            } catch (ValidationException e) {
                throw new RpcValidationException("RPC Validation failed: "+e.getRpcError().getErrorMessage(), e, e.getRpcError(), false, true);
            }
        }

        @Override
        public List<Notification> processRequest(NetconfClientInfo clientInfo, NetconfRpcRequest request,
                NetconfRpcResponse response) throws RpcProcessException {
            return null;
        }
    }

    @Test
    public void testMustConstraint_MultipleInnerPredicatesWith_OR_Operation() throws Exception {
        // create multiple hardware components
        String request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "    <hardware xmlns=\"schema-mount-test\">" +
                "            <component>" +
                "                <name>Board</name>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:board</class>" +
                "              </component>" +
                "            <component>" +
                "                <name>FSM1</name>" +
                "                <parent>Board</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>FSM2</name>" +
                "                <parent>Board</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>FSM3</name>" +
                "                <parent>Board</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</class>" +
                "              </component>" +
                "            <component>" +
                "                <name>SFP1</name>" +
                "                <parent>FSM1</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>UPLINK_PORT1</name>" +
                "                <parent>SFP1</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>SFP2</name>" +
                "                <parent>FSM2</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>UPLINK_PORT2</name>" +
                "                <parent>SFP2</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>SFP3</name>" +
                "                <parent>FSM3</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</class>" +
                "              </component>" +
                "              <component>" +
                "                <name>UPLINK_PORT3</name>" +
                "                <parent>SFP3</parent>" +
                "                <class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</class>" +
                "              </component>" +
                "       </hardware>" +
                "       <ethernet xmlns=\"schema-mount-test\">" +
                "          <manual>" +
                "             <port-layer-if>UPLINK_PORT2</port-layer-if>" +
                "             <speed>eth-if-speed-10gb</speed>" +
                "          </manual>" +
                "       </ethernet>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);

        // Verify GET response
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:ethernet xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:manual>\n" +
                "                     <smt:port-layer-if>UPLINK_PORT2</smt:port-layer-if>\n" +
                "                     <smt:speed>eth-if-speed-10gb</smt:speed>\n" +
                "                  </smt:manual>\n" +
                "               </smt:ethernet>\n" +
                "               <smt:hardware xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:board</smt:class>\n" +
                "                     <smt:name>Board</smt:name>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM1</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM2</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM3</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP1</smt:name>\n" +
                "                     <smt:parent>FSM1</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT1</smt:name>\n" +
                "                     <smt:parent>SFP1</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP2</smt:name>\n" +
                "                     <smt:parent>FSM2</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT2</smt:name>\n" +
                "                     <smt:parent>SFP2</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP3</smt:name>\n" +
                "                     <smt:parent>FSM3</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT3</smt:name>\n" +
                "                     <smt:parent>SFP3</smt:parent>\n" +
                "                  </smt:component>\n" +
                "               </smt:hardware>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // change the port-layer-if to 'UPLINK_PORT5' (don't have such hardware/components) --> must constraint should be failed
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "       <ethernet xmlns=\"schema-mount-test\">" +
                "          <manual>" +
                "             <port-layer-if>UPLINK_PORT5</port-layer-if>" +
                "          </manual>" +
                "       </ethernet>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        NetConfResponse netconfResponse = editConfigAsFalse(request);

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:ethernet/smt:manual/smt:speed", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ((current()/../port-layer-if = /hardware/component[class='bbf-hwt:transceiver-link' and parent= /hardware/component[class='bbf-hwt:transceiver' and (parent='FSM1'or parent='FSM2'or parent='FSM3')]/name]/name) and current()='eth-if-speed-10gb')", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());

        // modify the speed leaf value --> must constraint should be failed
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "       <ethernet xmlns=\"schema-mount-test\">" +
                "          <manual>" +
                "             <speed>eth-if-speed-50gb</speed>" +
                "          </manual>" +
                "       </ethernet>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        netconfResponse = editConfigAsFalse(request);

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:ethernet/smt:manual/smt:speed", netconfResponse.getErrors().get(0).getErrorPath());
        assertEquals("Violate must constraints: ((current()/../port-layer-if = /hardware/component[class='bbf-hwt:transceiver-link' and parent= /hardware/component[class='bbf-hwt:transceiver' and (parent='FSM1'or parent='FSM2'or parent='FSM3')]/name]/name) and current()='eth-if-speed-10gb')", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());

        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "       <ethernet xmlns=\"schema-mount-test\">" +
                "          <manual>" +
                "             <port-layer-if>UPLINK_PORT3</port-layer-if>" +
                "             <speed>eth-if-speed-10gb</speed>" +
                "          </manual>" +
                "       </ethernet>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        editConfig(request);

        // Verify GET response
        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">\n" +
                "         <validation:xml-subtree>\n" +
                "            <validation:plugType>PLUG-1.0</validation:plugType>\n" +
                "            <schemaMountPoint xmlns=\"urn:org:bbf2:pma:validation\">\n" +
                "               <smt:ethernet xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:manual>\n" +
                "                     <smt:port-layer-if>UPLINK_PORT3</smt:port-layer-if>\n" +
                "                     <smt:speed>eth-if-speed-10gb</smt:speed>\n" +
                "                  </smt:manual>\n" +
                "               </smt:ethernet>\n" +
                "               <smt:hardware xmlns:smt=\"schema-mount-test\">\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:board</smt:class>\n" +
                "                     <smt:name>Board</smt:name>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM1</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM2</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:cage</smt:class>\n" +
                "                     <smt:name>FSM3</smt:name>\n" +
                "                     <smt:parent>Board</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP1</smt:name>\n" +
                "                     <smt:parent>FSM1</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT1</smt:name>\n" +
                "                     <smt:parent>SFP1</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP2</smt:name>\n" +
                "                     <smt:parent>FSM2</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT2</smt:name>\n" +
                "                     <smt:parent>SFP2</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver</smt:class>\n" +
                "                     <smt:name>SFP3</smt:name>\n" +
                "                     <smt:parent>FSM3</smt:parent>\n" +
                "                  </smt:component>\n" +
                "                  <smt:component>\n" +
                "                     <smt:class xmlns:bbf-hwt=\"urn:bbf:yang:bbf-hardware-types\">bbf-hwt:transceiver-link</smt:class>\n" +
                "                     <smt:name>UPLINK_PORT3</smt:name>\n" +
                "                     <smt:parent>SFP3</smt:parent>\n" +
                "                  </smt:component>\n" +
                "               </smt:hardware>\n" +
                "            </schemaMountPoint>\n" +
                "         </validation:xml-subtree>\n" +
                "      </validation:validation>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(m_server, m_clientInfo, response);

        // verify impact validation --> delete 'port-layer-if' leaf
        request = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "  <xml-subtree>" +
                "   <plugType>PLUG-1.0</plugType>" +
                "   <schemaMountPoint>" +
                "       <ethernet xmlns=\"schema-mount-test\">" +
                "          <manual>" +
                "             <port-layer-if xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"delete\">UPLINK_PORT3</port-layer-if>" +
                "          </manual>" +
                "       </ethernet>" +
                "   </schemaMountPoint>" +
                "  </xml-subtree>" +
                " </validation>";
        netconfResponse = editConfigAsFalse(request);

        assertEquals(1,netconfResponse.getErrors().size());
        assertEquals("/validation:validation/validation:xml-subtree[validation:plugType='PLUG-1.0']/validation:schemaMountPoint/smt:ethernet/smt:manual/smt:speed", netconfResponse.getErrors().get(0).getErrorPath());        assertEquals("Violate must constraints: ((current()/../port-layer-if = /hardware/component[class='bbf-hwt:transceiver-link' and parent= /hardware/component[class='bbf-hwt:transceiver' and (parent='FSM1'or parent='FSM2'or parent='FSM3')]/name]/name) and current()='eth-if-speed-10gb')", netconfResponse.getErrors().get(0).getErrorMessage());
        assertEquals(NetconfRpcErrorTag.OPERATION_FAILED, netconfResponse.getErrors().get(0).getErrorTag());
        assertEquals("must-violation", netconfResponse.getErrors().get(0).getErrorAppTag());
    }
}
