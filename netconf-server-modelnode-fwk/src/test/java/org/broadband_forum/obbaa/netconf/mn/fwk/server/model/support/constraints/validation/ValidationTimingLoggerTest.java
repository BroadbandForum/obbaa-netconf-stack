package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.After;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.ValidationTimingLogger.ConstraintType;

public class ValidationTimingLoggerTest {
	
	@After
	public void tearDown() {
		ValidationTimingLogger.clearCurrentTimeForTest();
	}

	@Test
	public void testValidationTimingLogger() {
		ValidationTimingLogger.setCurrentTimeForTest(10000L);
		ValidationTimingLogger.start();		
		ValidationTimingLogger.startPhase("phase1");
		
		ValidationTimingLogger.setCurrentTimeForTest(10500L);
		ValidationTimingLogger.endPhase("phase1");
		
		ValidationTimingLogger.startPhase("phase2");
		
		ValidationTimingLogger.setCurrentTimeForTest(10800L);
		ValidationTimingLogger.startConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		ValidationTimingLogger.setCurrentTimeForTest(11200L);
		ValidationTimingLogger.endConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		ValidationTimingLogger.setCurrentTimeForTest(12000L);
		ValidationTimingLogger.endPhase("phase2");
		
		ValidationTimingLogger.finish();
		
		assertEquals(2000L, ValidationTimingLogger.getTotalTime().longValue());
		
		String timingsString = ValidationTimingLogger.getTimingsString();
		String expectedTimings = 
				"Total: 2000 ms, Sum of logged phases: 2000 ms (100.00%), phase1: 500 ms (25.00%), phase2: 1500 ms (75.00%), specific constraint timings:\n"
			  + "  WHEN ../hardware-type = SX16F : 400 ms (20.00%)\n";
		assertEquals(expectedTimings, timingsString);
		
		Map<String, Long> timings = ValidationTimingLogger.getTimings();
		assertEquals(2000L, timings.get("#TOTAL").longValue());
		assertEquals(500L, timings.get("phase1").longValue());
		assertEquals(1500L, timings.get("phase2").longValue());
		assertEquals(400L, timings.get("#CONSTRAINT#WHEN#../hardware-type = SX16F").longValue());
		
		ValidationTimingLogger.close();
	}
}
