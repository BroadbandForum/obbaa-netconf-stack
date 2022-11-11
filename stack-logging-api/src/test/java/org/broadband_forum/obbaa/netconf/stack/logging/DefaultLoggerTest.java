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

import static org.broadband_forum.obbaa.netconf.stack.logging.DefaultLogger.GLOBAL_LOG_TRUNCATE_LIMIT;
import static org.broadband_forum.obbaa.netconf.stack.logging.DefaultLogger.TRACE_DEBUG_LOG_TRUNCATE_LIMIT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class DefaultLoggerTest {
    private DefaultLogger m_wrapper;
    @Mock
    private Logger m_slf4jLogger;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        m_wrapper = (DefaultLogger) new LoggerFactory.DefaultLoggerFactoryImpl().getLogger(DefaultLoggerTest.class.getName(), "ut", "debug", "global");
        m_wrapper.setSlfLogger(m_slf4jLogger);
    }

	@Test
	public void testIsTruncateTraceDebugLogEnabled() {
		DefaultLogger wrapper = (DefaultLogger) new LoggerFactory.DefaultLoggerFactoryImpl()
				.getLogger(DefaultLoggerTest.class.getName(), "protocol.nbi.netconf", "debug", "global");
		wrapper.setSlfLogger(m_slf4jLogger);

		assertFalse(wrapper.isTruncateTraceDebugLogEnabled());

		wrapper = (DefaultLogger) new LoggerFactory.DefaultLoggerFactoryImpl()
				.getLogger(DefaultLoggerTest.class.getName(), "ibn", "debug", "global");
		wrapper.setSlfLogger(m_slf4jLogger);

		assertTrue(wrapper.isTruncateTraceDebugLogEnabled());
	}

	@Test
    public void testIsTruncateGlobalLogEnabled() {
        DefaultLogger wrapper = (DefaultLogger) new LoggerFactory.DefaultLoggerFactoryImpl()
                .getLogger(DefaultLoggerTest.class.getName(), "protocol.nbi.netconf", "debug", "global");
        wrapper.setSlfLogger(m_slf4jLogger);

        assertFalse(wrapper.isTruncateGlobalLogEnabled());

        wrapper = (DefaultLogger) new LoggerFactory.DefaultLoggerFactoryImpl()
                .getLogger(DefaultLoggerTest.class.getName(), "ibn", "debug", "global");
        wrapper.setSlfLogger(m_slf4jLogger);

        assertTrue(wrapper.isTruncateGlobalLogEnabled());
    }

    @Test
    public void testWrapperDelegatesOnAllMethods(){
        String param1 = "param1";
        String param2 = "param2";
        String param3 = "param3";
        RuntimeException exception = new RuntimeException("exception");
        
        String msg = "trace message";
		m_wrapper.trace(msg);
        verify(m_slf4jLogger).trace(msg);

        m_wrapper.setSlfLogger(m_slf4jLogger);
        String longMessage = getLongMessage(msg);
        String truncatedMessage = StringUtils.abbreviate(longMessage, TRACE_DEBUG_LOG_TRUNCATE_LIMIT);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage);
        verify(m_slf4jLogger).trace(truncatedMessage);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage);
        verify(m_slf4jLogger).trace(longMessage);
        
        m_wrapper.setSlfLogger(m_slf4jLogger);
        String veryLongMessage = getLongMessage(msg);
        String truncatedVeryLongMessage = StringUtils.abbreviate(veryLongMessage, GLOBAL_LOG_TRUNCATE_LIMIT);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage);
        verify(m_slf4jLogger).trace(veryLongMessage);

        m_wrapper.trace("trace message with 2 params {} {}", param1, param2);
        verify(m_slf4jLogger).trace("trace message with 2 params {} {}", param1, param2);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage, param1, param2);
        verify(m_slf4jLogger).trace(truncatedMessage, param1, param2);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage, param1, param2);
        verify(m_slf4jLogger).trace(longMessage, param1, param2);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage, param1, param2);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage, param1, param2);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage, param1, param2);
        verify(m_slf4jLogger).trace(veryLongMessage, param1, param2);

        m_wrapper.trace("trace message with 2 params {} {} and exception", param1, param2, exception);
        verify(m_slf4jLogger).trace("trace message with 2 params {} {} and exception", param1, param2, exception);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage, param1, param2, exception);
        verify(m_slf4jLogger).trace(truncatedMessage, param1, param2, exception);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage, param1, param2, exception);
        verify(m_slf4jLogger).trace(longMessage, param1, param2, exception);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage, param1, param2, exception);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage, param1, param2, exception);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage, param1, param2, exception);
        verify(m_slf4jLogger).trace(veryLongMessage, param1, param2, exception);

        m_wrapper.trace("trace message with 3 params {} {} {}", param1, param2, param3);
        verify(m_slf4jLogger).trace("trace message with 3 params {} {} {}", param1, param2, param3);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage, param1, param2, param3);
        verify(m_slf4jLogger).trace(truncatedMessage, param1, param2, param3);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage, param1, param2, param3);
        verify(m_slf4jLogger).trace(longMessage, param1, param2, param3);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage, param1, param2, param3);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage, param1, param2, param3);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage, param1, param2, param3);
        verify(m_slf4jLogger).trace(veryLongMessage, param1, param2, param3);

        m_wrapper.trace("trace message with 3 params {} {} {} and exception", param1, param2, param3, exception);
        verify(m_slf4jLogger).trace("trace message with 3 params {} {} {} and exception", param1, param2, param3, exception);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage, param1, param2, param3, exception);
        verify(m_slf4jLogger).trace(truncatedMessage, param1, param2, param3, exception);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage, param1, param2, param3, exception);
        verify(m_slf4jLogger).trace(longMessage, param1, param2, param3, exception);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage, param1, param2, param3, exception);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage, param1, param2, param3, exception);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage, param1, param2, param3, exception);
        verify(m_slf4jLogger).trace(veryLongMessage, param1, param2, param3, exception);

        m_wrapper.trace(msg, exception);
        verify(m_slf4jLogger).trace(msg, exception);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.trace(longMessage, exception);
        verify(m_slf4jLogger).trace(truncatedMessage, exception);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.trace(longMessage, exception);
        verify(m_slf4jLogger).trace(longMessage, exception);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.trace(veryLongMessage, exception);
        verify(m_slf4jLogger).trace(truncatedVeryLongMessage, exception);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.trace(veryLongMessage, exception);
        verify(m_slf4jLogger).trace(veryLongMessage, exception);

        m_wrapper.isTraceEnabled();
        verify(m_slf4jLogger).isTraceEnabled();

        msg = "debug message";
        m_wrapper.debug(msg);
        verify(m_slf4jLogger).debug(msg);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.debug(longMessage);
        verify(m_slf4jLogger).debug(truncatedMessage);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.debug(longMessage);
        verify(m_slf4jLogger).debug(longMessage);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.debug(veryLongMessage);
        verify(m_slf4jLogger).debug(truncatedVeryLongMessage);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.debug(veryLongMessage);
        verify(m_slf4jLogger).debug(veryLongMessage);

        m_wrapper.debug(msg, exception);
        verify(m_slf4jLogger).debug(msg, exception);
        
        // Long Message Truncate Behavior
        m_wrapper.setTruncateTraceDebugLogs(true);
        m_wrapper.debug(longMessage, exception);
        verify(m_slf4jLogger).debug(truncatedMessage, exception);
        
        m_wrapper.setTruncateTraceDebugLogs(false);
        m_wrapper.debug(longMessage, exception);
        verify(m_slf4jLogger).debug(longMessage, exception);

        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.debug(veryLongMessage, exception);
        verify(m_slf4jLogger).debug(truncatedVeryLongMessage, exception);

        m_wrapper.setTruncateGlobalLogs(false);
        m_wrapper.debug(veryLongMessage, exception);
        verify(m_slf4jLogger).debug(veryLongMessage, exception);

        m_wrapper.isDebugEnabled();
        verify(m_slf4jLogger).isDebugEnabled();

        m_wrapper.info("info message");
        verify(m_slf4jLogger).info("info message");
        m_wrapper.info("info message", exception);
        verify(m_slf4jLogger).info("info message", exception);
        m_wrapper.isInfoEnabled();
        verify(m_slf4jLogger).isInfoEnabled();
        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.info(veryLongMessage, exception);
        verify(m_slf4jLogger).info(truncatedVeryLongMessage, exception);
        m_wrapper.setTruncateGlobalLogs(false);

        m_wrapper.warn("warn message");
        verify(m_slf4jLogger).warn("warn message");
        m_wrapper.warn("warn message", exception);
        verify(m_slf4jLogger).warn("warn message", exception);
        m_wrapper.isWarnEnabled();
        verify(m_slf4jLogger).isWarnEnabled();
        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.warn(veryLongMessage, exception);
        verify(m_slf4jLogger).warn(truncatedVeryLongMessage, exception);
        m_wrapper.setTruncateGlobalLogs(false);

        m_wrapper.error("error message");
        verify(m_slf4jLogger).error("error message");
        m_wrapper.error("error message", exception);
        verify(m_slf4jLogger).error("error message", exception);
        m_wrapper.isErrorEnabled();
        verify(m_slf4jLogger).isErrorEnabled();
        // Very Long Message Truncate Behavior
        m_wrapper.setTruncateGlobalLogs(true);
        m_wrapper.error(veryLongMessage, exception);
        verify(m_slf4jLogger).error(truncatedVeryLongMessage, exception);
        m_wrapper.setTruncateGlobalLogs(false);

        SensitiveObjectWrapper data = m_wrapper.sensitiveData("sensitive data");
        assertEquals("sensitive data", data.toString());
    }

	private String getLongMessage(String msg) {
        if (m_wrapper.getTruncateTraceDebugLogs()) {
            // stuff message to simulate long message
            while (msg.length() < 2500) {
                msg += msg;
            }
        } else if (m_wrapper.getTruncateGlobalLogs()) {
            // stuff message to simulate long message
            while (msg.length() < 20100) {
                msg += msg;
            }
        }

		return msg;
	}
}
