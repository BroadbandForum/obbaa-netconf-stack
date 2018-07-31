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

package org.broadband_forum.obbaa.netconf.stack.logging;

import static org.mockito.Mockito.verify;

import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class DefaultLoggerTest {
    private AdvancedLogger m_wrapper;
    @Mock
    private Logger m_slf4jLogger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_wrapper = new LoggerFactory.DefaultLoggerFactoryImpl().getLogger(DefaultLoggerTest.class.getName(), "ut",
                "debug", "global");
        DefaultLogger wrapper = (DefaultLogger) Proxy.getInvocationHandler(m_wrapper);
        wrapper.setSlf4jLogger(m_slf4jLogger);
    }

    @Test
    public void testWrapperDelegatesOnAllMethods() {
        String param1 = "param1";
        String param2 = "param2";
        String param3 = "param3";
        RuntimeException exception = new RuntimeException("exception");

        m_wrapper.trace("trace message");
        verify(m_slf4jLogger).trace("trace message");
        m_wrapper.trace("trace message with 2 params {} {}", param1, param2);
        verify(m_slf4jLogger).trace("trace message with 2 params {} {}", param1, param2);
        m_wrapper.trace("trace message with 2 params {} {} and exception", param1, param2, exception);
        verify(m_slf4jLogger).trace("trace message with 2 params {} {} and exception", param1, param2, exception);
        m_wrapper.trace("trace message with 3 params {} {} {}", param1, param2, param3);
        verify(m_slf4jLogger).trace("trace message with 3 params {} {} {}", param1, param2, param3);
        m_wrapper.trace("trace message with 3 params {} {} {} and exception", param1, param2, param3, exception);
        verify(m_slf4jLogger).trace("trace message with 3 params {} {} {} and exception", param1, param2, param3,
                exception);
        m_wrapper.trace("trace message", exception);
        verify(m_slf4jLogger).trace("trace message", exception);
        m_wrapper.isTraceEnabled();
        verify(m_slf4jLogger).isTraceEnabled();

        m_wrapper.debug("debug message");
        verify(m_slf4jLogger).debug("debug message");
        m_wrapper.debug("debug message", exception);
        verify(m_slf4jLogger).debug("debug message", exception);
        m_wrapper.isDebugEnabled();
        verify(m_slf4jLogger).isDebugEnabled();

        m_wrapper.info("info message");
        verify(m_slf4jLogger).info("info message");
        m_wrapper.info("info message", exception);
        verify(m_slf4jLogger).info("info message", exception);
        m_wrapper.isInfoEnabled();
        verify(m_slf4jLogger).isInfoEnabled();

        m_wrapper.warn("warn message");
        verify(m_slf4jLogger).warn("warn message");
        m_wrapper.warn("warn message", exception);
        verify(m_slf4jLogger).warn("warn message", exception);
        m_wrapper.isWarnEnabled();
        verify(m_slf4jLogger).isWarnEnabled();

        m_wrapper.error("error message");
        verify(m_slf4jLogger).error("error message");
        m_wrapper.error("error message", exception);
        verify(m_slf4jLogger).error("error message", exception);
        m_wrapper.isErrorEnabled();
        verify(m_slf4jLogger).isErrorEnabled();
    }
}
