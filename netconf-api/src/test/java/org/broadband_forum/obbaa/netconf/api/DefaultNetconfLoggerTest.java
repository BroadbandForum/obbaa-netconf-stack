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

package org.broadband_forum.obbaa.netconf.api;

import static org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger.NBI_REQUEST_MSG;
import static org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger.NBI_RESPONSE_MSG;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.broadband_forum.obbaa.netconf.api.logger.DefaultNetconfLogger;
import org.broadband_forum.obbaa.netconf.api.messages.GetRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.SensitiveObjectWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultNetconfLoggerTest {

    @Mock
    private AdvancedLogger m_innerLogger;
    private DefaultNetconfLogger m_logger;

    @Before
    public void setUp() throws Exception {
        m_logger = new DefaultNetconfLogger(m_innerLogger);
        doAnswer(invocationOnMock -> new SensitiveObjectWrapper(invocationOnMock.getArguments()[0])).when(m_innerLogger).sensitiveData(anyObject());
    }

    @Test
    public void testLoggerIsCalledForRequest() throws NetconfMessageBuilderException {
        verifyZeroInteractions(m_innerLogger);

        m_logger.logRequest("host", "port", "user", "1", new GetRequest().setMessageId("4").getRequestDocument());

        verify(m_innerLogger).debug(eq(NBI_REQUEST_MSG+" {}/{} ( {} ) session-id {} \n {} \n"), eq(sensitive("host")), eq(sensitive("port")),
                eq(sensitive("user")), eq(sensitive("1")),eq("<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"4\">\n" +
                        "   <get/>\n" +
                        "</rpc>\n"));
    }

    @Test
    public void testLoggerIsCalledForResponse() throws NetconfMessageBuilderException {
        verifyZeroInteractions(m_innerLogger);

        m_logger.logResponse("host", "port", "user", "1", new NetConfResponse().setMessageId("4").getResponseDocument(),
                new GetRequest().setMessageId("4"));

        verify(m_innerLogger).debug(eq(NBI_RESPONSE_MSG+" {}/{} ( {} ) session-id {} \n {} \n"), eq(sensitive("host")), eq(sensitive("port")),
                eq(sensitive("user")), eq(sensitive("1")),eq("<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1" +
                        ".0\" message-id=\"4\"/>\n"));
    }
    private SensitiveObjectWrapper sensitive(Object obj) {
        return new SensitiveObjectWrapper(obj);
    }
}
