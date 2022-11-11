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
import static org.junit.Assert.assertTrue;

import static junit.framework.TestCase.assertNull;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractSchemaMountTest;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class SkipImpactOnCreateCTNUsageTest extends AbstractSchemaMountTest {
    public static final String IMPACT_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
            " <validation xmlns=\"urn:org:bbf2:pma:validation\">" +
            "  <xml-subtree>" +
            "  <plugType>PLUG-1.0</plugType>" +
            "   <schemaMountPoint>" +
            "    <schemaMount xmlns=\"schema-mount-test\">" +
            "     <leaf1>leaf1</leaf1>" +
            "     <container1>" +
            "      <list1> " +
            "         <key>%s</key>" +
            "         <leaf3>20</leaf3>" +
            "         <leaf4>20</leaf4>" +
            "         <leafListMinElements>1</leafListMinElements>" +
            "         <type>%s</type>" +
            "      </list1>" +
            "      <list1> " +
            "         <key>%s</key>" +
            "         <leaf3>20</leaf3>" +
            "         <leaf4>20</leaf4>" +
            "         <leafListMinElements>1</leafListMinElements>" +
            "         <type>%s</type>" +
            "      </list1>" +
            "     </container1>" +
            "     <channelpair>" +
            "      <channelgroup-ref>%s</channelgroup-ref>" +
            "     </channelpair>" +
            "    </schemaMount>" +
            "   </schemaMountPoint>" +
            "  </xml-subtree>" +
            " </validation>";


    @Test
    public void testMustHintsUseActualOperationFromCTN(){
        TimingLogger.c_storeTimingLogger.set(true);
        String request = String.format(IMPACT_REQUEST, "cg1", "test", "cg2", "test", "cg1");
        assertImpactValidationsWereNotDone(request);
        List<TimingLogger> timingLoggers;

        request = String.format(IMPACT_REQUEST, "cg1", "test", "cg2", "test", "cg1");
        assertImpactValidationsWereNotDone(request);

        request = String.format(IMPACT_REQUEST, "cg2", "test", "cg3", "test", "cg1");
        assertImpactValidationsWereNotDone(request);

        request = String.format(IMPACT_REQUEST, "cg3", "test", "cg4", "test", "cg2");
        assertImpactValidationsWereNotDone(request);

        //in this case the type is being modified for cg4, so a impact validation should be done
        request = String.format(IMPACT_REQUEST, "cg3", "test", "cg4", "test2", "cg3");
        TimingLogger.c_timings.remove();
        editConfig(request);
        timingLoggers = TimingLogger.c_timings.get();
        boolean validationDone = false;
        TimingLogger logger = null;
        for (TimingLogger timingLogger : timingLoggers) {
            if(timingLogger.getConstraintTime(TimingLogger.ConstraintType.IMPACT.toString() + "/MUST",
                    "/schemaMount/container1/list1/type -impacts-> /schemaMount1/channelgroup-ref1 -with-constraint-> " +
                            "/smt:schemaMount/smt:container1/smt:list1[smt:key=current()]\n" +
                            "/smt:type='test'") != null){
                validationDone= true;
                logger = timingLogger;
                break;
            }
        }
        assertTrue(validationDone);
        assertEquals(new Long(1), logger.getInvocationCount(TimingLogger.ConstraintType.IMPACT.toString() + "/MUST",
                "/schemaMount/container1/list1/type -impacts-> /schemaMount1/channelgroup-ref1 -with-constraint-> " +
                        "/smt:schemaMount/smt:container1/smt:list1[smt:key=current()]\n" +
                        "/smt:type='test'"));

        TimingLogger.c_timings.remove();
        TimingLogger.c_storeTimingLogger.remove();
    }

    private void assertImpactValidationsWereNotDone(String request) {
        editConfig(request);
        List<TimingLogger> timingLoggers = TimingLogger.c_timings.get();
        for (TimingLogger timingLogger : timingLoggers) {
            assertNull(timingLogger.getConstraintTime(TimingLogger.ConstraintType.IMPACT.toString() + "/MUST",
                    "/schemaMount/container1/list1/type -impacts-> /schemaMount1/channelgroup-ref1 -with-constraint-> " +
                            "/smt:schemaMount/smt:container1/smt:list1[smt:key=current()]\n" +
                            "/smt:type='test'"));
        }
        TimingLogger.c_timings.remove();
    }


}
