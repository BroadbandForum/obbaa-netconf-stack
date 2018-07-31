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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.TestActionSubSystem;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.ActionResponse;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class ActionValidatorTest extends AbstractDataStoreValidatorTest {
    private static final String YANG_FILE = "/datastorevalidatortest/yangs/test-action.yang";
    private static final String YANG_FILE1 = "/datastorevalidatortest/yangs/ietf-yang-types.yang";
    private static final String NAMESPACE = "urn:example:test-action";
    private static final QName ACTION_CONTAINER = QName.create(NAMESPACE, "2015-12-14", "test-action-container");
    private static final SchemaPath ACTION_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, ACTION_CONTAINER);
    private static final String DEFAULT_XML = "/datastorevalidatortest/yangs/test-action-default.xml";
    private static final String PREFIX = "test";

    private NetConfResponse m_response = new ActionResponse().setMessageId("1");

    protected NetConfServerImpl getNcServer() {
        RpcRequestConstraintParser rpcValidator = new RpcRequestConstraintParser(m_schemaRegistry, m_modelNodeDsm,
                m_expValidator);
        return new NetConfServerImpl(m_schemaRegistry, rpcValidator);
    }

    protected List<String> getYang() {
        return Arrays.asList(YANG_FILE1, YANG_FILE);
    }

    protected String getXml() {
        return DEFAULT_XML;
    }

    protected SubSystem getSubSystem() {
        return new TestActionSubSystem(m_schemaRegistry);
    }

    protected void addRootNodeHelpers() {
        ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode
                (ACTION_CONTAINER_SCHEMA_PATH);
        ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry,
                m_modelNodeDsm);
        m_rootModelNodeAggregator.addModelServiceRootHelper(ACTION_CONTAINER_SCHEMA_PATH, containerHelper);

    }

    protected void getSchemaRegistry(List<YangTextSchemaSource> yangFiles) throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, new NoLockService());
    }

    private Element getActionResponseElement() throws NetconfMessageBuilderException {
        String response = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<test:reset-finished-at xmlns:test=\"urn:example:test-action\">2014-07-29T13:42:00Z</test:reset" +
                "-finished-at>" +
                "</data>";
        Document document = DocumentUtils.stringToDocument(response);
        return document.getDocumentElement();
    }

    @Test
    public void testValidActionInList() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:action-list>" +
                "<test:name>apache</test:name>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getData());
    }

    @Test
    public void testValidActionInContainer() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:container-reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:container-reset>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getData());
    }

    @Test
    public void testInvalidNotExistedAction() throws NetconfMessageBuilderException, ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:container-reset1>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:container-reset1>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("No matched action found on the models", error.getErrorMessage());
    }

    @Test
    public void testValidActionInGrouping() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:grouping-reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:grouping-reset>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getData());

    }

    @Test
    public void testValidActionInAugment() throws Exception {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:augmented-reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:augmented-reset>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";

        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        TestUtil.assertXMLEquals(getActionResponseElement(), m_response.getData());
    }

    @Test
    public void testInvalidActionWithoutListKey() throws NetconfMessageBuilderException, ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:action-list>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Application));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.MISSING_ELEMENT));
        assertEquals("/test:test-action-container/test:action-list", error.getErrorPath());
        assertEquals("Expected list key(s) [name] is missing", error.getErrorMessage());
    }

    @Test
    public void testInvalidActionWithinNotification() throws NetconfMessageBuilderException, ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:test-notification>" +
                "<test:action-list>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-notification>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        Map<String, String> prefixToNsMap = new HashMap<>();
        prefixToNsMap.put(PREFIX, NAMESPACE);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("/test:test-action-container/test:test-notification", error.getErrorPath());
        assertEquals(prefixToNsMap, error.getErrorPathNsByPrefix());
        assertEquals("Notification Element test-notification should not exist within Action Tree", error
                .getErrorMessage());
    }

    @Test
    public void testInvalidActionWithinRPC() throws NetconfMessageBuilderException, ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:test-rpc>" +
                "<test:action-list>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:test-rpc>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));

        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        Map<String, String> prefixToNsMap = new HashMap<>();
        prefixToNsMap.put(PREFIX, NAMESPACE);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("/test:test-action-container/test:test-rpc", error.getErrorPath());
        assertEquals(prefixToNsMap, error.getErrorPathNsByPrefix());
        assertEquals("RPC Element test-rpc should not exist within Action Tree", error.getErrorMessage());
    }

    @Test
    public void testInvalidActionWithinAction() throws NetconfMessageBuilderException, ModelNodeInitException {
        getModelNode();
        initialiseInterceptor();
        String req = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">" +
                "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">" +
                "<test:test-action-container xmlns:test=\"urn:example:test-action\">" +
                "<test:container-reset>" +
                "<test:action-list>" +
                "<test:name>apache</test:name>" +
                "<test:reset>" +
                "<test:reset-at>2014-07-29T13:42:00Z</test:reset-at>" +
                "</test:reset>" +
                "</test:action-list>" +
                "</test:container-reset>" +
                "</test:test-action-container>" +
                "</action>" +
                "</rpc>";


        ActionRequest validActionRequest = DocumentToPojoTransformer.getAction(DocumentUtils.stringToDocument(req));
        m_server.onAction(m_clientInfo, validActionRequest, (ActionResponse) m_response);
        NetconfRpcError error = m_response.getErrors().get(0);
        assertTrue(error.getErrorType().equals(NetconfRpcErrorType.Protocol));
        assertTrue(error.getErrorTag().equals(NetconfRpcErrorTag.BAD_ELEMENT));
        assertEquals("Multiple action element exists within RPC", error.getErrorMessage());
    }

}
