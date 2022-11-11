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

package org.broadband_forum.obbaa.netconf.server.model;

import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity.Error;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag.ACCESS_DENIED;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType.Application;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;


@RunWith(MockitoJUnitRunner.class)
public class CompositeSubSystemTest {

    public static final String RESOURCE_IS_NOT_ALLOCATED = "Access to the requested data model is denied because authorization failed";

    private CompositeSubSystem m_composite;
    @Mock
    private SubSystem m_subsystem1;
    @Mock
    private SubSystem m_subsystem2;
    @Mock
    private SubSystem m_subsystem3;
    @Mock
    private Map<SchemaPath, List<ChangeTreeNode>> m_change1;
    @Mock
    private Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> m_attributes;
    @Mock
    private NetconfQueryParams m_queryParams;
    @Mock
    StateAttributeGetContext m_stateContext;

    @Before
    public void setUp() {
        m_composite = new CompositeSubSystemImpl();
        m_composite.register(m_subsystem1);
        m_composite.register(m_subsystem2);
    }

    @Test
    public void testRegisteredSubsystemsAreInvoked() throws SubSystemValidationException {
        verifyZeroInteractions(m_subsystem1);
        verifyZeroInteractions(m_subsystem2);
        m_composite.preCommit(m_change1);
        verify(m_subsystem1).preCommit(m_change1);
        verify(m_subsystem2).preCommit(m_change1);
        verifyNoMoreInteractions(m_subsystem1);
        verifyNoMoreInteractions(m_subsystem1);
    }

    @Test
    public void testSubsystemNotRegisteredIsNotInvoked() throws SubSystemValidationException {
        verifyZeroInteractions(m_subsystem3);
        m_composite.preCommit(m_change1);
        verifyZeroInteractions(m_subsystem3);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetrieveStateAttributesNotSupported() throws SubSystemValidationException, GetAttributeException {
            m_composite.retrieveStateAttributes(m_attributes, m_queryParams);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetrieveStateAttributesWithStateContextNotSupported() throws SubSystemValidationException, GetAttributeException {
            m_composite.retrieveStateAttributes(m_attributes, m_queryParams,m_stateContext);
    }

    @Test
    public void testNoSubSystemInvokedAfterSubSystemThrowsException() throws SubSystemValidationException, GetAttributeException {
        NetconfRpcError nre = new NetconfRpcError(ACCESS_DENIED, Application, Error, RESOURCE_IS_NOT_ALLOCATED);
        doThrow(new SubSystemValidationException(nre)).when(m_subsystem1).preCommit(m_change1);
        try {
            m_composite.preCommit(m_change1);
            fail("expected a exception");
        } catch (SubSystemValidationException e) {
            assertEquals(RESOURCE_IS_NOT_ALLOCATED, e.getMessage());
        }
        verifyZeroInteractions(m_subsystem2);
    }

    @After
    public void tearDown() throws Exception {
        m_composite.unRegister(m_subsystem1);
        m_composite.unRegister(m_subsystem2);
    }
}
