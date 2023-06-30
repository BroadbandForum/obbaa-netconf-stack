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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.auth.spi.AuthorizationHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Operation;
import org.broadband_forum.obbaa.netconf.server.ssh.auth.AccessDeniedException;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class AbstractSubSystemTest {

    public static final String NAMESPACE = "namespace";
    public static final String NODENAME = "name";
    public static final String CHILD_LIST = "childlist";
    private DummySubsystem m_dummySubSystem;
    private TestSubsystem m_testSubsystem;
    private ModelNodeId m_modelNodeId;
    private Element m_resultFromSubSystem1;
    private Element m_resultFromSubSystem2;
    private AuthorizationHandler m_authorizationHandler;

    @Before
    public void setup(){
        m_modelNodeId = new ModelNodeId("/container=rootContainer/container=list/name=xyz", NAMESPACE);
        m_authorizationHandler = mock(AuthorizationHandler.class);
        m_dummySubSystem = new DummySubsystem();
        m_testSubsystem = new TestSubsystem(m_authorizationHandler);
    }

    @Test
    public void testRetrieveStateAttributes_CachesCorrectResponse() throws GetAttributeException, IOException, SAXException {
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = new HashMap<>();
        List<FilterNode> filterNodes = new ArrayList<>();
        FilterMatchNode matchNode1 = new FilterMatchNode(NODENAME, NAMESPACE,"Test1");
        FilterNode filterNode1 = new FilterNode(Collections.EMPTY_LIST,Collections.singletonList(matchNode1),
                Collections.EMPTY_LIST, false, NAMESPACE, CHILD_LIST,null);
        FilterMatchNode matchNode2 = new FilterMatchNode(NODENAME, NAMESPACE,"Test2");
        FilterNode filterNode2 = new FilterNode(Collections.EMPTY_LIST,Collections.singletonList(matchNode2),
                Collections.EMPTY_LIST, false, NAMESPACE, CHILD_LIST,null);
        filterNodes.add(filterNode1);
        filterNodes.add(filterNode2);
        Pair<List<QName>,List<FilterNode>> pair = new Pair(Collections.EMPTY_LIST, filterNodes);
        attributes.put(m_modelNodeId, pair);
        NetconfQueryParams queryParams = mock(NetconfQueryParams.class);
        StateAttributeGetContext stateContext = new StateAttributeGetContext();
        m_dummySubSystem.retrieveStateAttributes(attributes, queryParams,stateContext);

        StoredFilters storedFilters = stateContext.getStoredFilterNodesForSS(m_dummySubSystem);
        Map<FilterNode, List<Element>> filterNodeElementMap = storedFilters.get(m_modelNodeId);
        assertXMLEquals(m_resultFromSubSystem1, filterNodeElementMap.get(filterNode1).get(0));
        assertXMLEquals(m_resultFromSubSystem2, filterNodeElementMap.get(filterNode2).get(0));
    }

    @Test
    public void testRetrieveStateAttributes_CachesSelectNodes() throws Exception {
        SubSystem subSystemSpy = spy(m_dummySubSystem);
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = new HashMap<>();
        List<FilterNode> inputFilterNodes = new ArrayList<>();

        FilterNode inputFilterNode = new FilterNode("wigig-stat", NAMESPACE);
        inputFilterNode.addSelectNode("name", NAMESPACE);
        inputFilterNode.addSelectNode("accesspoint", NAMESPACE);
        inputFilterNodes.add(inputFilterNode);

        Pair<List<QName>,List<FilterNode>> pair = new Pair(Collections.EMPTY_LIST, inputFilterNodes);
        attributes.put(m_modelNodeId, pair);
        NetconfQueryParams queryParams = mock(NetconfQueryParams.class);
        Map<ModelNodeId, List<Element>> stateValues = new HashMap<>();

        Element expectedResponse1 = TestUtil.parseXml("<wigig-stat xmlns=\"namespace\">\n" +
                " <name>WLAN-1</name>\n" +
                " <accesspoint>\n" +
                "  <associated-device>\n" +
                "   <mac-address>08:c5:e1:9a:3d:9d</mac-address>\n" +
                "  </associated-device>\n" +
                " </accesspoint>\n" +
                "</wigig-stat>");
        Element expectedResponse2 = TestUtil.parseXml("<wigig-stat xmlns=\"namespace\">\n" +
                " <name>WLAN-2</name>\n" +
                " <accesspoint>\n" +
                "  <associated-device>\n" +
                "   <mac-address>08:c5:e1:9a:3d:9d</mac-address>\n" +
                "  </associated-device>\n" +
                " </accesspoint>\n" +
                "</wigig-stat>");

        List<Element> elements = new ArrayList<>();
        elements.add(expectedResponse1);
        elements.add(expectedResponse2);
        stateValues.put(m_modelNodeId,elements);

        doReturn(stateValues).when(subSystemSpy).retrieveStateAttributes(attributes,queryParams);
        StateAttributeGetContext stateContext = new StateAttributeGetContext();
        subSystemSpy.retrieveStateAttributes(attributes,queryParams,stateContext);

        StoredFilters storedFilters = stateContext.getStoredFilterNodesForSS(subSystemSpy);
        Map<FilterNode, List<Element>> filterNodeElementMap = storedFilters.get(m_modelNodeId);
        assertEquals(2,filterNodeElementMap.get(inputFilterNode).size());
        assertXMLEquals(expectedResponse1, filterNodeElementMap.get(inputFilterNode).get(0));
        assertXMLEquals(expectedResponse2, filterNodeElementMap.get(inputFilterNode).get(1));
    }

    @Test
    public void testSubSystemIsHitOnce_SubsystemRespondsForOneOfTheFilters() throws Exception{
        SubSystem subSystemSpy = spy(m_dummySubSystem);
        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes = new HashMap<>();
        List<FilterNode> inputFilterNodes = new ArrayList<>();

        FilterNode hardwareStateFilterNode = new FilterNode("hardware-state", NAMESPACE);
        FilterNode componentFilterNode = new FilterNode("component", NAMESPACE);
        hardwareStateFilterNode.addContainmentNode(componentFilterNode);
        componentFilterNode.addMatchNode("name",NAMESPACE,"Board");
        componentFilterNode.addSelectNode("mfg-name",NAMESPACE);
        inputFilterNodes.add(hardwareStateFilterNode);

        FilterNode systemStateFilterNode = new FilterNode("system-state", NAMESPACE);
        systemStateFilterNode.addContainmentNode("clock",NAMESPACE);
        inputFilterNodes.add(systemStateFilterNode);

        Pair<List<QName>,List<FilterNode>> pair = new Pair(Collections.EMPTY_LIST, inputFilterNodes);
        attributes.put(m_modelNodeId, pair);
        NetconfQueryParams queryParams = mock(NetconfQueryParams.class);
        Map<ModelNodeId, List<Element>> stateValues = new HashMap<>();

        Element subsystemResponse = TestUtil.parseXml( "<hardware-state xmlns=\"namespace\">\n" +
                                                            "   <component>\n" +
                                                            "    <name>Board</name>\n" +
                                                            "    <mfg-name>ALCL</mfg-name>\n" +
                                                            "   </component>\n" +
                                                            "  </hardware-state>");

        List<Element> elements = new ArrayList<>();
        elements.add(subsystemResponse);
        stateValues.put(m_modelNodeId,elements);

        doReturn(stateValues).when(subSystemSpy).retrieveStateAttributes(attributes,queryParams);
        doReturn(Collections.EMPTY_MAP).when(subSystemSpy).retrieveStateAttributes(Collections.singletonMap(m_modelNodeId,new Pair<>(Collections.emptyList(),Collections.emptyList())),queryParams);
        StateAttributeGetContext stateContext = new StateAttributeGetContext();
        Map<ModelNodeId, List<Element>> firstInvocation = subSystemSpy.retrieveStateAttributes(attributes,queryParams,stateContext);

        Map<ModelNodeId, List<Element>> secondInvocation = subSystemSpy.retrieveStateAttributes(attributes,queryParams,stateContext);

        assertEquals(firstInvocation.size(), secondInvocation.size());
        assertEquals(firstInvocation.get(m_modelNodeId).size(), secondInvocation.get(m_modelNodeId).size());

        assertXMLEquals(subsystemResponse, firstInvocation.get(m_modelNodeId).get(0));
        assertXMLEquals(subsystemResponse, secondInvocation.get(m_modelNodeId).get(0));

        StoredFilters storedFilters = stateContext.getStoredFilterNodesForSS(subSystemSpy);
        Map<FilterNode, List<Element>> filterNodeElementMap = storedFilters.get(m_modelNodeId);

        assertXMLEquals(subsystemResponse, filterNodeElementMap.get(hardwareStateFilterNode).get(0));
        assertTrue(storedFilters.isFilterNodeWithEmptyResponse(m_modelNodeId,systemStateFilterNode));

    }

    @Test
    public void testCheckRequiredPermissions_isPermitted() throws Exception {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1, 1);
        when(m_authorizationHandler.isPermittedAll(1, "NBI_RESIDUAL")).thenReturn(true);
        try {
            m_testSubsystem.checkRequiredPermissions(clientInfo, Operation.EDIT.getType());
        } catch (AccessDeniedException e) {
            fail("No AccessDeniedException should be thrown here");
        }
    }

    @Test
    @Ignore("byPassAuthorization() is set to true for OBBAA")
    public void testCheckRequiredPermissions_notPermitted() throws Exception {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1, 1);
        when(m_authorizationHandler.isPermittedAll(1, "NBI_RESIDUAL")).thenReturn(false);
        try {
            m_testSubsystem.checkRequiredPermissions(clientInfo, Operation.EDIT.getType());
            fail("AccessDeniedException expected");
        } catch (AccessDeniedException e) {
            assertEquals("Operation 'edit-config' not authorized for user 'test'", e.getMessage());
        }
    }

    @Test
    @Ignore("byPassAuthorization() is set to true for OBBAA")
    public void testCheckRequiredPermissions_authorizatonHanderThrowsException() throws Exception {
        NetconfClientInfo clientInfo = new NetconfClientInfo("test", 1, 1);
        when(m_authorizationHandler.isPermittedAll(1, "NBI_RESIDUAL")).thenThrow(new Exception());
        try {
            m_testSubsystem.checkRequiredPermissions(clientInfo, Operation.EDIT.getType());
            fail("AccessDeniedException expected");
        } catch (AccessDeniedException e) {
            assertEquals("Operation 'edit-config' not authorized for user 'test'", e.getMessage());
        }
    }

    @Test
    @Ignore("byPassAuthorization() is set to true for OBBAA")
    public void testCheckRequiredPermissions_byPassPermissionCheck() throws Exception {
        m_testSubsystem = new TestSubsystem(m_authorizationHandler) {
            @Override
            protected boolean byPassAuthorization() {
                return true;
            }
        };
        try {
            m_testSubsystem.checkRequiredPermissions(null, null);
        } catch (AccessDeniedException e) {
            fail("No AccessDeniedException should be thrown here");
        }
    }

    private class TestSubsystem extends AbstractSubSystem {
        public TestSubsystem(AuthorizationHandler authorizationHandler) {
            super(authorizationHandler);
        }
    }

    private class DummySubsystem extends AbstractSubSystem{

        public DummySubsystem() {
            super();
        }

        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                                                       NetconfQueryParams queryParams) throws GetAttributeException {
            Map<ModelNodeId, List<Element>> m_expectedStateInfo;

            try {
                 m_resultFromSubSystem1 = TestUtil.parseXml("<childlist xmlns=\"namespace\">\n" +
                        " <name>Test1</name>\n" +
                        " <type>sameType</type>\n" +
                        "</childlist>");
                 m_resultFromSubSystem2 = TestUtil.parseXml("<childlist xmlns=\"namespace\">\n" +
                        " <name>Test2</name>\n" +
                        " <type>sameType</type>\n" +
                        "</childlist>");
            } catch (Exception e){
                throw new RuntimeException(e);
            }
            m_expectedStateInfo = new HashMap<>();
            List<Element> result = new ArrayList<>();
            result.add(m_resultFromSubSystem1);
            result.add(m_resultFromSubSystem2);
            m_expectedStateInfo.put(m_modelNodeId,result);
            return m_expectedStateInfo;
        }
    }
}
