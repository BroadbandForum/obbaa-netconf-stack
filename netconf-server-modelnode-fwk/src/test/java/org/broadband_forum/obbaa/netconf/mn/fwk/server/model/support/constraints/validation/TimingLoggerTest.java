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

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger.ConstraintType;
import org.junit.After;
import org.junit.Test;

public class TimingLoggerTest {
	
	@After
	public void tearDown() {
		TimingLogger.clearCurrentTimeForTest();
	}


	@Test
	public void testTimingLoggerWithoutPhases() {
		TimingLogger.setCurrentTimeForTest(10000L);
		TimingLogger.start();

		TimingLogger.setCurrentTimeForTest(12000L);
		TimingLogger finishedLogger = TimingLogger.finish();

		assertEquals(2000L, finishedLogger.getTotalTime().longValue());

		assertEquals("Total: 2000 ms, Sum of logged phases: 0 ms (0.00%)", finishedLogger.toString());

		Map<String, Long> timings = finishedLogger.m_timings;
		assertEquals(2000L, timings.get("#TOTAL").longValue());
		assertEquals(0L, timings.get("#TOTAL_LOGGED").longValue());

		TimingLogger.close();
	}

	@Test
	public void testMultipleStartAndFinish() {
		TimingLogger.setCurrentTimeForTest(10000L);
		TimingLogger.start(); //outer start at 10th sec
		TimingLogger.setCurrentTimeForTest(15000L);
		TimingLogger.start(); // inner start at 15th sec
		TimingLogger.setCurrentTimeForTest(16000L);

		TimingLogger finishedInner = TimingLogger.finish(); // inner finish at 16th sec
		TimingLogger finishedOuter = TimingLogger.finish(); // outer finish at 16th sec

		assertEquals("Total: 1000 ms, Sum of logged phases: 0 ms (0.00%)", finishedInner.toString());
		assertEquals("Total: 6000 ms, Sum of logged phases: 0 ms (0.00%)", finishedOuter.toString());

		assertEquals(1000L, finishedInner.getTotalTime().longValue());
		assertEquals(6000L, finishedOuter.getTotalTime().longValue());

		TimingLogger.close();
	}

	@Test
	public void testValidationTimingLogger() {
		TimingLogger.setCurrentTimeForTest(10000L);
		TimingLogger.start();
		TimingLogger.startPhase("phase1", "test description");
		
		TimingLogger.setCurrentTimeForTest(10500L);
		TimingLogger.endPhase("phase1");
		
		TimingLogger.startPhase("phase2");
		
		TimingLogger.setCurrentTimeForTest(10800L);
		TimingLogger.startConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		TimingLogger.setCurrentTimeForTest(11200L);
		TimingLogger.endConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		TimingLogger.setCurrentTimeForTest(12000L);
		TimingLogger.endPhase("phase2");

		TimingLogger finishedLogger = TimingLogger.finish();

		assertEquals(2000L, finishedLogger.getTotalTime().longValue());
		
		String timingsString = finishedLogger.toString();
		String expectedTimings = 
				"Total: 2000 ms, Sum of logged phases: 2000 ms (100.00%)<-- LOOK HERE,\nphase2: 1500 ms (75.00%)<-- LOOK HERE " +
						"invocation-count: 1,\nphase1: 500 ms (25.00%)<-- LOOK HERE desc { test description } invocation-count: 1, specific constraint timings:\n" +
						"  WHEN ../hardware-type = SX16F : 400 ms (20.00%)<-- LOOK HERE invocation-count: 1\n";
		assertEquals(expectedTimings, timingsString);
		
		Map<String, Long> timings = finishedLogger.m_timings;
		assertEquals(2000L, timings.get("#TOTAL").longValue());
		assertEquals(500L, timings.get("phase1").longValue());
		assertEquals(1500L, timings.get("phase2").longValue());
		assertEquals(400L, timings.get("#CONSTRAINT#WHEN#../hardware-type = SX16F").longValue());

		TimingLogger.close();
	}
	
	@Test
	public void testValidationTimingLogger_WithoutStartAndFinish() {
		TimingLogger.setCurrentTimeForTest(10000L);
		TimingLogger.startPhase("phase1", "test description");
		
		TimingLogger.setCurrentTimeForTest(10500L);
		TimingLogger.endPhase("phase1");
		
		TimingLogger.startPhase("phase2");
		
		TimingLogger.setCurrentTimeForTest(10800L);
		TimingLogger.startConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		TimingLogger.setCurrentTimeForTest(11200L);
		TimingLogger.endConstraint(ConstraintType.WHEN, "../hardware-type = SX16F");
		
		TimingLogger.setCurrentTimeForTest(12000L);
		TimingLogger.endPhase("phase2");

		TimingLogger.close();
	}

	@Test
	public void testValidationTimingLoggerForRecursivePhases() {
		TimingLogger.setCurrentTimeForTest(10000L);
		TimingLogger.start();

		TimingLogger.startPhase("phase1", "test description");
		TimingLogger.setCurrentTimeForTest(10500L);
		TimingLogger.endPhase("phase1");

		recursiveFunction(5);

		TimingLogger finishedLogger = TimingLogger.finish();

		assertEquals(4500L, finishedLogger.getTotalTime().longValue());

		String timingsString = finishedLogger.toString();
		String expectedTimings =
				"Total: 4500 ms, Sum of logged phases: 500 ms (11.11%)<-- LOOK HERE,\n" +
						"recursivePhase: 4000 ms (88.89%)<-- LOOK HERE invocation-count: 5,\n" +
						"phase1: 500 ms (11.11%)<-- LOOK HERE desc { test description } invocation-count: 1";
		assertEquals(expectedTimings, timingsString);

		Map<String, Long> timings = finishedLogger.m_timings;
		assertEquals(4500L, timings.get("#TOTAL").longValue());
		assertEquals(500L, timings.get("phase1").longValue());
		assertEquals(4000L, timings.get("recursivePhase").longValue());

		TimingLogger.close();
	}

	private void recursiveFunction(int count) {
		--count;
		TimingLogger.startPhase("recursivePhase");
		if(count != 0) {
			recursiveFunction(count);
		}
		TimingLogger.setCurrentTimeForTest(10500L + count * 1000);
		TimingLogger.endPhase("recursivePhase", false);
	}
}
