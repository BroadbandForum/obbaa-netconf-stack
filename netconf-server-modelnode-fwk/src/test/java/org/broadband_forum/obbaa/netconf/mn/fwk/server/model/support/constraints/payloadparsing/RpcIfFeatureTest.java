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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.payloadparsing;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class RpcIfFeatureTest extends AbstractDataStoreValidatorTest {

    private DummyRpcHandler m_dummyIfFeatureRpcHandler;
    private DummyRpcHandler m_dummyIfFeatureOutputRpcHandler;
    private RpcPayloadConstraintParser m_rpcConstraintParser;
    private ModelNodeDataStoreManager m_modelNodeDsm;
    protected static final QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "testRematch");
    protected static final SchemaPath VALIDATION_SCHEMA_PATH1 = SchemaPath.create(true, VALIDATION_QNAME);
    protected NetConfServerImpl m_server;

    @Before
    public void setup() throws SchemaBuildException, ModelNodeInitException {
        super.setUp();
        m_dummyIfFeatureRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testIfFeature"));
        m_dummyIfFeatureOutputRpcHandler = new DummyRpcHandler(new RpcName("urn:org:bbf2:pma:validation", "testOutputIfFeature"));
        m_rpcConstraintParser = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm, m_expValidator, null);
        getModelNode();
    }

    @Test
    public void testRpcIfFeature() throws Exception {
        String xmlPath = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + " <validation:testIfFeature xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafName>test</validation:testLeafName>" 
                + " </validation:testIfFeature>"
                + "</rpc>";
        Element element = DocumentUtils.stringToDocument(xmlPath).getDocumentElement();
        NetconfRpcRequest request = new NetconfRpcRequest();
        request.setRpcInput(DocumentUtils.getInstance().getFirstElementChildNode(element));
        request.setMessageId("1");
        TimingLogger.withStartAndFinish(() -> m_dummyIfFeatureRpcHandler.validate(m_rpcConstraintParser, request));
    }

    @Test
    public void testRpcOutputIfFeature() throws Exception {
        String rpcResponse = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + " <validation:test xmlns:validation=\"urn:org:bbf2:pma:validation\">"
                + " <validation:testLeafName>test</validation:testLeafName>" 
                + " </validation:test>" 
                + "</rpc-reply>";
        Element element1 = DocumentUtils.stringToDocument(rpcResponse).getDocumentElement();
        NetconfRpcResponse netconfResponse = new NetconfRpcResponse();
        netconfResponse.setMessageId("1");
        TestUtil.addOutputElements(element1, netconfResponse);
        netconfResponse.setRpcName(new RpcName("urn:org:bbf2:pma:validation", "testOutputIfFeature"));
        m_dummyIfFeatureOutputRpcHandler.validate(m_rpcConstraintParser, netconfResponse);
    }

    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        String rpc_yang_file = "/datastorevalidatortest/yangs/datastore-validator-test.yang";
        String rpc_yang_file1 = "/datastorevalidatortest/yangs/ietf-inet-types.yang";
        String rpc_yang_file2 = "/datastorevalidatortest/yangs/datastore-validator-grouping-test.yang";
        String rpc_yang_file3 = "/datastorevalidatortest/yangs/dummy-extension.yang";
        String yang_file = "/datastorevalidatortest/yangs/test-interfaces.yang";
        YangTextSchemaSource featuretest_yang = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(rpc_yang_file));
        YangTextSchemaSource featuretest_yang1 = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(rpc_yang_file1));
        YangTextSchemaSource featuretest_yang2 = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(rpc_yang_file2));
        YangTextSchemaSource featuretest_yang3 = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(rpc_yang_file3));
        YangTextSchemaSource featuretest_yang4 = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(yang_file));
        QName test_feature1 = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "local-storage");
        Set<QName> feature_set = new HashSet<QName>();
        feature_set.add(test_feature1);
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(featuretest_yang, featuretest_yang1, featuretest_yang2, featuretest_yang3, featuretest_yang4), feature_set, Collections.emptyMap(), new NoLockService());
    }
    
}
