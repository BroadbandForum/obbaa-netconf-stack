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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;


public class DefaultLogger implements AdvancedLogger {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DefaultLogger.class);

    private Logger m_slf4jLogger;//NOSONAR
    private static final String SENSITIVE_DATA = "sensitiveData";
    private static final String SET_LOG_CALL_BUFFER = "setLogCallBuffer";
    private static final String REMOVE_LOG_CALL_BUFFER = "removeLogCallBuffer";
    
    private static final String ENV_TRUNCATE_TRACE_DEBUG_LOGS = "TRUNCATE_TRACE_DEBUG_LOGS";
    private static final boolean TRUNCATE_TRACE_DEBUG_LOGS_DEFAULT = true;
    private static final String ENV_TRUNCATE_GLOBAL_LOGS = "TRUNCATE_GLOBAL_LOGS";
    private static final boolean TRUNCATE_GLOBAL_LOGS_DEFAULT = true;
    protected static final int TRACE_DEBUG_LOG_TRUNCATE_LIMIT = 2000;
    protected static final int GLOBAL_LOG_TRUNCATE_LIMIT = 20000;

	private String m_application;
	private static final String PROTOCOL = "protocol.";
	private boolean m_truncateTraceDebugLogs;
	private boolean m_truncateGlobalLogs;
    
    // for testing purposes, to be able to retrieve the log calls afterwards
    private List<LogCallEntry> m_logCallBuffer = null;

    public DefaultLogger(String category, String application) {
        m_slf4jLogger = org.slf4j.LoggerFactory.getLogger(category);
        m_application = application;
        m_truncateTraceDebugLogs = isTruncateTraceDebugLogEnabled();
        m_truncateGlobalLogs = isTruncateGlobalLogEnabled();
    }

    @Override
    public Boolean isTraceEnabled() {
        return m_slf4jLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (m_truncateTraceDebugLogs) {
            msg = processLongMessage(msg);
        } else if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (m_truncateTraceDebugLogs) {
            msg = processLongMessage(msg);
        } else if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.trace(msg, t);
    }

    @Override
    public Boolean isDebugEnabled() {
        return m_slf4jLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (m_truncateTraceDebugLogs) {
            msg = processLongMessage(msg);
        } else if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (m_truncateTraceDebugLogs) {
            format = processLongMessage(format);
        } else if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (m_truncateTraceDebugLogs) {
            msg = processLongMessage(msg);
        } else if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.debug(msg, t);
    }

    @Override
    public Boolean isInfoEnabled() {
        return m_slf4jLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.info(msg, t);
    }

    @Override
    public Boolean isWarnEnabled() {
        return m_slf4jLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.warn(msg, t);
    }

    @Override
    public Boolean isErrorEnabled() {
        return m_slf4jLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        if (m_truncateGlobalLogs) {
            format = processVeryLongMessage(format);
        }
        m_slf4jLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        if (m_truncateGlobalLogs) {
            msg = processVeryLongMessage(msg);
        }
        m_slf4jLogger.error(msg, t);
    }

    @Override
    public SensitiveObjectWrapper sensitiveData(Object object) {
        return new SensitiveObjectWrapper(object);
    }

    @Override
    public void setLogCallBuffer(List<LogCallEntry> buffer) {

    }

    @Override
    public void removeLogCallBuffer() {

    }

    /**
     * For UTs
     * @param slf4jLogger
     */
    public void setSlfLogger(Logger slf4jLogger) {
        m_slf4jLogger = slf4jLogger;
    }

    private String processLongMessage(String origLog) {
        return StringUtils.abbreviate(origLog, TRACE_DEBUG_LOG_TRUNCATE_LIMIT);
    }

    private String processVeryLongMessage(String origLog) {
        return StringUtils.abbreviate(origLog, GLOBAL_LOG_TRUNCATE_LIMIT);
    }

	protected boolean isTruncateTraceDebugLogEnabled() {
		if (m_application.startsWith(PROTOCOL)) {
			return false;
		}
		String value = System.getenv(ENV_TRUNCATE_TRACE_DEBUG_LOGS);
		if(!StringUtils.isEmpty(value)) {
			return Boolean.parseBoolean(value);
		}
		return TRUNCATE_TRACE_DEBUG_LOGS_DEFAULT;
	}

	protected boolean isTruncateGlobalLogEnabled() {
        if (m_application.startsWith(PROTOCOL)) {
            return false;
        }
        String value = System.getenv(ENV_TRUNCATE_GLOBAL_LOGS);
        if(!StringUtils.isEmpty(value)) {
            return Boolean.parseBoolean(value);
        }
        return TRUNCATE_GLOBAL_LOGS_DEFAULT;
    }
    
    /**
     * For UTs
     */

    boolean getTruncateTraceDebugLogs() {
        return m_truncateTraceDebugLogs;
    }

    void setTruncateTraceDebugLogs(boolean enabled) {
        m_truncateTraceDebugLogs = enabled;
    }

    boolean getTruncateGlobalLogs() {
        return m_truncateGlobalLogs;
    }

    void setTruncateGlobalLogs(boolean enabled) {
        m_truncateGlobalLogs = enabled;
    }
}
