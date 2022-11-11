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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class DSMTimingLogger {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DSMTimingLogger.class, LogAppNames.NETCONF_STACK);

    private static final String TOTAL_LOGGED_KEY = "#TOTAL_LOGGED";
    private static final String START_SUFFIX = "#start";

    private static final DecimalFormat twoDecimals = new DecimalFormat("0.00");

    public DSMTimingLogger() {

    }

    private static ThreadLocal<Stack<DSMTimingLogger>> m_timingLogger = ThreadLocal.withInitial(() -> new Stack<>());

    protected static void close() {
        m_timingLogger.remove();
    }

    public final Map<String, Long> m_timings = new LinkedHashMap<>();
    private final Map<String, Long> m_invocationCount = new LinkedHashMap<>();

    public static void start() {
        DSMTimingLogger DSMTimingLogger = new DSMTimingLogger();
        m_timingLogger.get().push(DSMTimingLogger);
    }

    public static DSMTimingLogger finish() {
        Stack<DSMTimingLogger> stack = m_timingLogger.get();
        if(!stack.isEmpty()) {
            DSMTimingLogger logger = stack.pop();
            return logger;
        }
        LOGGER.debug("DSM timing Logger was empty");
        return new DSMTimingLogger();
    }

    public static void startPhase(String phase) {
        if(m_timingLogger.get().isEmpty()){
            return;
        }
        DSMTimingLogger logger = m_timingLogger.get().peek();
        String startkey = getStartKey(phase);
        if (!logger.m_timings.containsKey(startkey)) {
            logger.m_timings.put(startkey, getCurrentTime());
        }

        if (!logger.m_invocationCount.containsKey(startkey)) {
            logger.m_invocationCount.put(startkey, 1l);
        } else {
            logger.m_invocationCount.put(startkey, logger.m_invocationCount.get(startkey) + 1);
        }
    }

    public static void endPhase(String phase) {
        endPhase(phase, true);
    }

    public static void endPhase(String phase, boolean addToTotal) {
        if(m_timingLogger.get().isEmpty()){
            return;
        }
        DSMTimingLogger logger = m_timingLogger.get().peek();
        String startkey = getStartKey(phase);
        if (logger.m_timings.containsKey(startkey)) {
            long starttime = logger.m_timings.get(startkey);
            long newtime = getCurrentTime() - starttime;
            logger.addTimeToPhase(phase, newtime);
            if (addToTotal) {
                logger.addTimeToPhase(TOTAL_LOGGED_KEY, newtime);
            }
            logger.m_timings.remove(startkey);
        } else {
            LOGGER.warn("Phase " + phase + " not started");
        }
    }

    private void addTimeToPhase(String phase, long newtime) {
        long loggedtime = 0;
        if (m_timings.containsKey(phase)) {
            loggedtime = m_timings.get(phase);
        }
        m_timings.put(phase, loggedtime + newtime);
    }

    private static String getStartKey(String phase) {
        return phase + START_SUFFIX;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        long totalLogged = 0;
        if(m_timings.containsKey(TOTAL_LOGGED_KEY)) {
            totalLogged = m_timings.get(TOTAL_LOGGED_KEY);
        }
        sb.append("Sum of logged phases: ");
        appendPhaseTime(sb, totalLogged, totalLogged);
        sb.append("\n");
        for (String phase : m_timings.keySet()) {
            long phasetime = m_timings.get(phase);
            sb.append(phase).append(": ");
            appendPhaseTime(sb, totalLogged, phasetime);
            Long count = m_invocationCount.get(getStartKey(phase));
            if(count != null) {
                appendInvocationCount(sb, count);
            }
            sb.append(",\n");
        }
        return sb.toString();
    }

    private void appendInvocationCount(StringBuffer sb, Long count) {
        sb.append(" invocation-count: ").append(count);
    }

    private void appendPhaseTime(StringBuffer sb, long totaltime, long phasetime) {
        double percentage = totaltime == 0 ? 0 : ((double) phasetime) * 100 / totaltime;
        sb.append(phasetime).append(" ms (");
        sb.append(twoDecimals.format(percentage)).append("%)");
    }


    private static long getCurrentTime() {
        return System.currentTimeMillis();
    }

}
