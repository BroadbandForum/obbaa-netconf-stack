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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl.GET_FINAL_FILTER;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfFilter;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.SubtreeFilterUtil;

public class NetconfServerImplTest {

    @Mock
    private SchemaRegistry schemaRegistry;
    private NetConfServerImpl m_server;
    private NetconfClientInfo m_clientInfo;
    private GetRequest m_getReq;
    @Mock
    private NetConfResponse m_response;
    @Mock
    private DataStore m_ds;
    @Mock
    private java.util.List<org.w3c.dom.Element> values;
    @Mock
    private SubtreeFilterUtil m_util;
    @Mock
    private NetconfFilter m_filter;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        m_getReq = new GetRequest();
        m_getReq.setFilter(m_filter);
        m_getReq.setMessageId("1");
        m_clientInfo = new NetconfClientInfo("ut", 1);
        when(m_ds.get(anyObject(), anyObject(), anyObject())).thenReturn(values);
        m_server = new NetConfServerImpl(schemaRegistry, null, m_util);
        m_server.setRunningDataStore(m_ds);
    }

    @Test
    public void tesGetUseSystemVar() {
        System.setProperty(GET_FINAL_FILTER, "false");
        m_server.onGet(m_clientInfo, m_getReq, m_response);
        verifyZeroInteractions(m_util);
        System.setProperty(GET_FINAL_FILTER, "true");
        m_server.onGet(m_clientInfo, m_getReq, m_response);
        verify(m_util).filter(anyObject(), eq(null));
        System.clearProperty(GET_FINAL_FILTER);
        verifyNoMoreInteractions(m_util);
    }

    @After
    public void tearDown() {
        System.clearProperty(GET_FINAL_FILTER);
    }
}
