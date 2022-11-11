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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class TimingLogger {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(TimingLogger.class, LogAppNames.NETCONF_STACK);
    private static ThreadLocal<String> c_phaseSuffix = ThreadLocal.withInitial(() -> "");
    private static ThreadLocal<String> c_contextPath = ThreadLocal.withInitial(() -> "");
    public static ThreadLocal<List<TimingLogger>> c_timings = ThreadLocal.withInitial(() -> new ArrayList<>());
    public static ThreadLocal<Boolean> c_storeTimingLogger = ThreadLocal.withInitial(() -> Boolean.FALSE);

    public Long getTotalTime() {
        return m_timings.containsKey(TOTAL_KEY) ? m_timings.get(TOTAL_KEY) : 0L;
    }

    public static enum ConstraintType {
		WHEN, MUST, SIZE, UNIQUE, LEAFREF, INSTANCEID, IMPACT, MANDATORY;
    }

    /**
     * Total time spent between {@link TimingLogger#start()} and {@link TimingLogger#finish()}
     */
    private static final String TOTAL_KEY = "#TOTAL";
    /**
     * Total time spent in all logged phases from {@link TimingLogger#start()} and {@link TimingLogger#finish()}
     * which is sum of all time spent between {@link TimingLogger#startPhase(String)} and {@link TimingLogger#endPhase(String)}
     *
     * Example:
     *
     * {@link TimingLogger#start()} ----------------------------------------------------|
     *                                                                                  |
     * {@link TimingLogger#startPhase(String phase_1)}  -----|                          |
     * {@link TimingLogger#endPhase(String phase_1)}         |                          |
     *                                                       |---- #TOTAL_LOGGED time   |-----#TOTAL time
     * {@link TimingLogger#startPhase(String phase_2)}       |                          |
     * {@link TimingLogger#endPhase(String phase_2)}   ------|                          |
     *                                                                                  |
     * {@link TimingLogger#finish()} ---------------------------------------------------|
     *
     */
    private static final String TOTAL_LOGGED_KEY = "#TOTAL_LOGGED";
    private static final String CONSTRAINT_KEY = "#CONSTRAINT";
    private static final String START_SUFFIX = "#start";

    private static final DecimalFormat twoDecimals = new DecimalFormat("0.00");

	private TimingLogger() {

	}

	private static ThreadLocal<Stack<TimingLogger>> m_timingLogger = new ThreadLocal<Stack<TimingLogger>>() {
		@Override
		protected Stack<TimingLogger> initialValue() {
			return new Stack<>();
		}
	};

	protected static void close() {
		m_timingLogger.remove();
	}

	public final Map<String, Long> m_timings = new LinkedHashMap<>();
    private final Map<String, Long> m_invocationCount = new LinkedHashMap<>();
    private final Map<String, Long> m_recursionCount = new LinkedHashMap<>();
    private final Map<String, String> m_descriptions = new HashMap<>();

    public static void start() {
        TimingLogger timingLogger = new TimingLogger();
        timingLogger.addTimeToPhase(TOTAL_LOGGED_KEY, 0);
        m_timingLogger.get().push(timingLogger);
        Map<String, Long> timings = timingLogger.m_timings;
        timings.put(getStartKey(TOTAL_KEY), getCurrentTime());
	}

    public static TimingLogger finish() {
        Stack<TimingLogger> stack = m_timingLogger.get();
        if(!stack.isEmpty()){
            TimingLogger logger = stack.pop();
            String startkey = getStartKey(TOTAL_KEY);
            long starttime = logger.m_timings.get(startkey);
            long totaltime = getCurrentTime() - starttime;
            logger.addTimeToPhase(TOTAL_KEY, totaltime);
            logger.m_timings.remove(startkey);
            if(c_storeTimingLogger.get()) {
                c_timings.get().add(logger);
            }
            return logger;
        }
        LOGGER.debug("edit timing Logger was empty");
       return new TimingLogger();
    }

    public static void startPhase(String phase, String description) {
        if (m_timingLogger.get().isEmpty()) {
            LOGGER.debug("Logger stack is empty");
	    return;
        }
        TimingLogger logger = m_timingLogger.get().peek();
        String startkey = getStartKey(phase);
        if (logger.m_timings.containsKey(startkey)) {
            logger.m_recursionCount.put(startkey, logger.m_recursionCount.get(startkey) + 1);
        } else {
            logger.m_timings.put(startkey, getCurrentTime());
            logger.m_recursionCount.put(startkey, 1l);
        }
        if (!logger.m_invocationCount.containsKey(startkey)) {
            logger.m_invocationCount.put(startkey, 1l);
        } else {
            logger.m_invocationCount.put(startkey, logger.m_invocationCount.get(startkey) + 1);
        }
        if (description != null) {
            logger.m_descriptions.put(phase, description);
        }
    }

    public static void startPhase(String phase) {
        startPhase(phase, null);
    }

    public static void endPhase(String phase) {
    	endPhase(phase, true);
    }

    public static void endPhase(String phase, boolean addToTotal) {
        if (m_timingLogger.get().isEmpty()) {
            LOGGER.debug("Logger stack is empty");
            return;
        }
        TimingLogger logger = m_timingLogger.get().peek();
        String startkey = getStartKey(phase);
        if(logger.m_timings.containsKey(startkey) && logger.m_recursionCount.containsKey(startkey)) {
            logger.m_recursionCount.put(startkey, logger.m_recursionCount.get(startkey) - 1);
            if (logger.m_recursionCount.get(startkey) == 0l) {
                long starttime = logger.m_timings.get(startkey);
                long newtime = getCurrentTime() - starttime;
                logger.addTimeToPhase(phase, newtime);
                if (addToTotal) {
                    logger.addTimeToPhase(TOTAL_LOGGED_KEY, newtime);
                }
                logger.m_timings.remove(startkey);
                logger.m_recursionCount.remove(startkey);
            }
        } else {
            LOGGER.warn("Phase " + phase + " not started");
        }
    }

    public static void startConstraint(ConstraintType type, String constraint) {
        startConstraint(type.toString(), constraint);
    }

    public static void startConstraint(String type, String constraint) {
        if(!"".equals(c_phaseSuffix.get())){
            type = type+"/"+c_phaseSuffix.get();
            constraint = constraint + " - on -> "+ c_contextPath.get();
        }
        startConstraint(type, constraint, Optional.of(""));
    }

    public static void startConstraint(String type, String constraint, Optional<String> description) {
        String key = getConstraintKey(type, constraint);
        startPhase(key);
        if(description.isPresent() &&!description.get().isEmpty() && !m_timingLogger.get().isEmpty()){
            TimingLogger logger = m_timingLogger.get().peek();
            logger.m_descriptions.put(key, description.get());
        }
    }

    private static String getConstraintKey(String type, String constraint) {
        return CONSTRAINT_KEY + "#" + type + "#" + constraint;
    }

    public Long getConstraintTime(String type, String constraint) {
        String key = getConstraintKey(type, constraint);
        return m_timings.get(key);
    }

    public Long getInvocationCount(String type, String constraint) {
        String key = getStartKey(getConstraintKey(type, constraint));
        return m_invocationCount.get(key);
    }

    public static void endConstraint(ConstraintType type, String constraint) {
    	endConstraint(type.toString(), constraint);
    }

    public static void endConstraint(String type, String constraint) {
        if(!"".equals(c_phaseSuffix.get())){
            type = type+"/"+c_phaseSuffix.get();
            constraint = constraint + " - on -> "+ c_contextPath.get();
        }
        endPhase(getConstraintKey(type, constraint), false);
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
    	if (m_timings.isEmpty()) {
    		return "";
    	}
        StringBuffer sb = new StringBuffer();
        long totaltime = m_timings.get(TOTAL_KEY);
        sb.append("Total: ").append(totaltime).append(" ms");
        long totallogged = m_timings.get(TOTAL_LOGGED_KEY);
        sb.append(", Sum of logged phases: ");
        appendPhaseTime(sb, totaltime, totallogged);
        List<String> constraintKeys = new ArrayList<>();
        List<TimingEntry> sortedList = getSortedEntries();
        Collections.sort(sortedList);
        int noOfEntries = sortedList.size();
        for (int i = 0; i < noOfEntries; i++) {
            TimingEntry timingEntry = sortedList.get(i);
            String phase = timingEntry.getKey();
            if (!phase.contains("#")) {
                long phasetime = m_timings.get(phase);
                sb.append(",\n").append(phase).append(": ");
                String desc = m_descriptions.get(phase);
                appendPhaseTime(sb, totaltime, phasetime, desc);
                Long count = m_invocationCount.get(getStartKey(phase));
                if (count != null) {
                    appendInvocationCount(sb, count);
                }
            } else if (phase.startsWith(CONSTRAINT_KEY)) {
                constraintKeys.add(phase);
            }
        }

        if (! constraintKeys.isEmpty()) {
            sb.append(", specific constraint timings:\n");
            for (TimingEntry timingEntry: sortedList) {
                String phase = timingEntry.getKey();
                if(constraintKeys.contains(phase)) {
                    String[] split = phase.split("#");
                    long phasetime = m_timings.get(phase);
                    sb.append("  ").append(split[2]).append(" ").append(split[3]).append(" : ");
                    appendPhaseTime(sb, totaltime, phasetime);
                    Long count = m_invocationCount.get(getStartKey(phase));
                    if(count != null) {
                        appendInvocationCount(sb, count);
                    }
                    String desc = m_descriptions.get(phase);
                    if(desc != null) {
                        appendDesc(sb, desc);
                    }
                    sb.append("\n");

                }
            }

        }
        return sb.toString();
    }

    private void appendDesc(StringBuffer sb, String desc) {
        sb.append(" desc { ").append(desc).append(" }");
    }

    private List<TimingEntry> getSortedEntries() {
        List<TimingEntry> sortedEntries = new ArrayList<>();
        for (Map.Entry<String, Long> entry : m_timings.entrySet()) {
            TimingEntry timingEntry = new TimingEntry(entry.getKey(), entry.getValue());
            sortedEntries.add(timingEntry);
        }
        return sortedEntries;
    }

    private void appendInvocationCount(StringBuffer sb, Long count) {
        sb.append(" invocation-count: ").append(count);
    }

    private void appendPhaseTime(StringBuffer sb, long totaltime, long phasetime, String description) {
        double percentage = ((double)phasetime) * 100 / totaltime;
        sb.append(phasetime).append(" ms (");
        sb.append(twoDecimals.format(percentage)).append("%)");
        if(percentage > 2) {
            sb.append("<-- LOOK HERE");
            if (description != null) {
                appendDesc(sb, description);
            }
        }
    }

    private void appendPhaseTime(StringBuffer sb, long totaltime, long phasetime) {
        appendPhaseTime(sb, totaltime, phasetime, null);
    }

    // UT support
    private static Long m_currentTimeForTest;

    static void setCurrentTimeForTest(long time) {
    	m_currentTimeForTest = time;
    }

    static void clearCurrentTimeForTest() {
    	m_currentTimeForTest = null;
    }

	private static long getCurrentTime() {
		if (m_currentTimeForTest != null) {
			return m_currentTimeForTest;
		}
		return System.currentTimeMillis();
	}

    public static <T> T withStartAndFinish(TimingLoggerTemplate<T> template) {
        try {
            TimingLogger.start();
            return template.run();
        } finally {
            TimingLogger.finish();
        }
    }

    public static void withStartAndFinish(TimingLoggerVoidTemplate template) {
        try {
            TimingLogger.start();
            template.run();
        } finally {
            TimingLogger.finish();
        }
    }


    public static <T> T withExtraInfo(String phaseSuffix, String contextPath, TimingLoggerTemplate<T> template) {
        try {
            TimingLogger.c_phaseSuffix.set(phaseSuffix);
            TimingLogger.c_contextPath.set(contextPath);
            return template.run();
        } finally {
            TimingLogger.c_phaseSuffix.remove();
            TimingLogger.c_contextPath.remove();
        }
    }

    public static void withExtraInfo(String phaseSuffix, String contextPath, TimingLoggerVoidTemplate template) {
        try {
            TimingLogger.c_phaseSuffix.set(phaseSuffix);
            TimingLogger.c_contextPath.set(contextPath);
            template.run();
        } finally {
            TimingLogger.c_phaseSuffix.remove();
            TimingLogger.c_contextPath.remove();
        }
    }

    private class TimingEntry implements Comparable<TimingEntry> {
        private final String m_key;
        private final Long m_timeTaken;

        public TimingEntry(String key, Long timeTaken) {
            m_key = key;
            m_timeTaken = timeTaken;
        }

        public String getKey() {
            return m_key;
        }

        public Long getTimeTaken() {
            return m_timeTaken;
        }

        @Override
        public int compareTo(TimingEntry other) {
            return other.m_timeTaken.compareTo(m_timeTaken);
        }

        @Override
        public String toString() {
            return "TimingEntry{" +
                    "m_key='" + m_key + '\'' +
                    ", m_timeTaken=" + m_timeTaken +
                    '}';
        }
    }
}
