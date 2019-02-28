package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class ValidationTimingLogger {
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ValidationTimingLogger.class, LogAppNames.NETCONF_STACK);
    
	public static enum ConstraintType {
		WHEN, MUST, SIZE, UNIQUE, LEAFREF, INSTANCEID;
	}
	
    private static final String TOTAL_KEY = "#TOTAL";
    private static final String TOTAL_LOGGED_KEY = "#TOTAL_LOGGED";
    private static final String CONSTRAINT_KEY = "#CONSTRAINT";
    private static final String START_SUFFIX = "#start";
    
    private static final DecimalFormat twoDecimals = new DecimalFormat("0.00");
	
	private ValidationTimingLogger() {
		
	}
	
	private static ThreadLocal<ValidationTimingLogger> m_timingLogger = new ThreadLocal<ValidationTimingLogger>() {
		@Override
		protected ValidationTimingLogger initialValue() {
			return new ValidationTimingLogger();
		}
	};

	public static void close() {
		m_timingLogger.remove();
	}
	
	private final Map<String, Long> m_timings = new LinkedHashMap<String, Long>();
	
	public static void start() {
		Map<String, Long> timings = m_timingLogger.get().m_timings;
		if (!timings.isEmpty()) {
			LOGGER.warn("Timings not empty, nested call? " + timings);
			timings.clear();
		}
		timings.put(getStartKey(TOTAL_KEY), getCurrentTime());
	}
    
    public static void finish() {
    	ValidationTimingLogger logger = m_timingLogger.get();
        String startkey = getStartKey(TOTAL_KEY);
        long starttime = logger.m_timings.get(startkey);
        long totaltime = getCurrentTime() - starttime;
        logger.addTimeToPhase(TOTAL_KEY, totaltime);
        logger.m_timings.remove(startkey);
    }
    
    public static void startPhase(String phase) {
    	ValidationTimingLogger logger = m_timingLogger.get();
        String startkey = getStartKey(phase);
        if (logger.m_timings.containsKey(startkey)) {
            LOGGER.warn("Overlapping phase " + phase);
        }
        else {
        	logger.m_timings.put(startkey, getCurrentTime());
        }
    }
    
    public static void endPhase(String phase) {
    	endPhaseInternal(phase, true);
    }

	private static void endPhaseInternal(String phase, boolean addToTotal) {
		ValidationTimingLogger logger = m_timingLogger.get();
        String startkey = getStartKey(phase);
        if (logger.m_timings.containsKey(startkey)) {
            long starttime = logger.m_timings.get(startkey);
            long newtime = getCurrentTime() - starttime;
            logger.addTimeToPhase(phase, newtime);
            if (addToTotal) {
            	logger.addTimeToPhase(TOTAL_LOGGED_KEY, newtime);
            }
            logger.m_timings.remove(startkey);
        }
        else {
            LOGGER.warn("Phase " + phase + " not started");
        }
	}
    
    public static void startConstraint(ConstraintType type, String constraint) {
    	startPhase(CONSTRAINT_KEY + "#" + type + "#" + constraint);
    }
    
    public static void endConstraint(ConstraintType type, String constraint) {
    	endPhaseInternal(CONSTRAINT_KEY + "#" + type + "#" + constraint, false);
    }

    private void addTimeToPhase(String phase, long newtime) {
        long loggedtime = 0;
        if (m_timings.containsKey(phase)) {
            loggedtime = m_timings.get(phase);
        }
        m_timings.put(phase, loggedtime + newtime);
    }
    
    public static Map<String, Long> getTimings() {
    	ValidationTimingLogger logger = m_timingLogger.get();
        return logger.m_timings;
    }
    
    public static String getTimingsString() {
    	ValidationTimingLogger logger = m_timingLogger.get();
    	return logger.toString();
    }
    
    public static Long getTotalTime() {
    	return getTimings().get(TOTAL_LOGGED_KEY);
    }
    
    private static String getStartKey(String phase) {
        return phase + START_SUFFIX;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        long totaltime = m_timings.get(TOTAL_KEY);
        sb.append("Total: ").append(totaltime).append(" ms");
        long totallogged = m_timings.get(TOTAL_LOGGED_KEY);
        sb.append(", Sum of logged phases: ");
        appendPhaseTime(sb, totaltime, totallogged);
        List<String> constraintKeys = new ArrayList<>();
        for (String phase: m_timings.keySet()) {
            if (! phase.contains("#")) {
                long phasetime = m_timings.get(phase);
                sb.append(", ").append(phase).append(": ");
                appendPhaseTime(sb, totaltime, phasetime);
            }
            else if (phase.startsWith(CONSTRAINT_KEY)) {
            	constraintKeys.add(phase);
            }
        }
        if (! constraintKeys.isEmpty()) {
        	sb.append(", specific constraint timings:\n");
        	for (String phase: constraintKeys) {
        		String[] split = phase.split("#");
        		long phasetime = m_timings.get(phase);
        		sb.append("  ").append(split[2]).append(" ").append(split[3]).append(" : ");
        		appendPhaseTime(sb, totaltime, phasetime);
        		sb.append("\n");
        	}
        }
        return sb.toString();
    }

    private void appendPhaseTime(StringBuffer sb, long totaltime, long phasetime) {
        double percentage = ((double)phasetime) * 100 / totaltime;
        sb.append(phasetime).append(" ms (");
        sb.append(twoDecimals.format(percentage)).append("%)");
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
}
