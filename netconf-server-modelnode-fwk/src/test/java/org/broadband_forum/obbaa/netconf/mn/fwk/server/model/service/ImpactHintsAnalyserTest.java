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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static java.util.Optional.empty;
import static java.util.Optional.of;

import static junit.framework.TestCase.assertEquals;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint.SKIP_IMPACT_ON_CREATE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils.getExpression;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class ImpactHintsAnalyserTest {
    static final Logger LOGGER = LogManager.getLogger(ImpactHintsAnalyserTest.class);

    private static Set<String> c_skippedConstraints;
    private static Set<String> c_notSkippedConstraints;

    private ImpactHintsAnalyser m_analyser;

    @BeforeClass
    public static void beforeClass() {
        c_skippedConstraints = new HashSet<>();
        c_notSkippedConstraints = new HashSet<>();
    }

    @AfterClass
    public static void printReport(){
        StringBuilder reportBuilder = new StringBuilder();
        reportBuilder.append("Sample constraints that will be getting SKIP_IMPACT_ON_CREATE are {\n");
        for (String constraint : c_skippedConstraints) {
            reportBuilder.append("  "+constraint).append("\n");
        }
        reportBuilder.append("}\n");
        reportBuilder.append("Sample constraints that will NOT be getting SKIP_IMPACT_ON_CREATE are { \n");
        for (String constraint : c_notSkippedConstraints) {
            reportBuilder.append("  "+constraint).append("\n");
        }
        reportBuilder.append("}\n");
        LOGGER.info(reportBuilder);
    }

    @Before
    public void setUp() {
        m_analyser = new ImpactHintsAnalyser();
    }
    private static List<YangTextSchemaSource> getYangs() {
        List<YangTextSchemaSource> yangs = new ArrayList<>();

        File yangDir = new File(AugmentWhenConditionAnalyserTest.class.getResource("/augmentwhenconditionanalysertest/yangs").getFile());
        for (File file : yangDir.listFiles()) {
            if (file.isFile()) {
                yangs.add(YangParserUtil.getYangSource(file.getPath()));
            }
        }
        return yangs;
    }


    @Test
    public void testNegativeCases(){
        testNegative("count(/if:interfaces/if:interface)");
        testNegative("sum(/if:interfaces/if:interface)");
        testNegative("some-other-function(/if:interfaces/if:interface)");
        testNegative("some-other-function(/if:interfaces/if:interface) != 'ianaift:ptm'");
        testNegative("some-other-function(/if:interfaces/if:interface) != 'ianaift:ptm'");
        testNegative("derived-from(sum-other-function(/if:interfaces/if:interface))");
        testNegative("derived-from-or-self (/hw:hardware\n" +
                "         /hw:component[hw:name=current() and count ( /if:interfaces/if:interface ) > 10]/hw:class,\n" +
                "         'ianahw:port')");
        testNegative("count()/../../if:type = 'bbf-xponift:channel-partition'");
        testNegative("current()/../../if:interface[if:name = 'bbf-xponift:channel-partition' and some-function()]");
        testNegative(" re-match(count(../interface),\"\\\\d{1,3}\\\\.\\\\d{1,3}\\\\.\\\\d{1,3}\\\\.\\\\d{1,3}\") ");
        testNegative("bit-is-set(sum(../bit), 'somebit')");
        testNegative("enum-value(count(../severity)) != 5");
    }

    @Test
    public void testPositiveCases(){
        testPositive("../if:type='ianaift:ptm'");
        testPositive("derived-from(../if:type='ianaift:ptm')");
        testPositive("derived-from-or-self(../if:type='ianaift:ptm')");
        testPositive("derived-from-or-self(if:type, 'ianaift:ethernetCsmacd') or\n" +
                "       derived-from-or-self(if:type, 'ianaift:ieee8023adLag') or\n" +
                "       derived-from-or-self(if:type, 'ianaift:ptm') or\n" +
                "       derived-from-or-self(if:type, 'bbfift:vlan-sub-interface')");
        testPositive("derived-from-or-self (/hw:hardware\n" +
                "         /hw:component[hw:name=current()]/hw:class,\n" +
                "         'ianahw:port')");
        testPositive("/if:interfaces/if:interface[if:name=current()]/\n" +
                "             if:type='bbf-xponift:channel-pair' and \n" +
                "            /if:interfaces/if:interface[if:name=current()]/\n" +
                "             bbf-xpon:channel-pair/bbf-xpon:channel-partition-ref=\n" +
                "             ../parent-ref");
        testPositive("current()/../../if:type = 'bbf-xponift:channel-partition'");
        testPositive("current()/../../if:interface[if:name = 'bbf-xponift:channel-partition']");
        testPositive("current()/../../if:type != 'bbf-xponift:channel-partition'");
        testPositive("bit-is-set(., 'somebit')");
        testPositive(" re-match(.,\"\\\\d{1,3}\\\\.\\\\d{1,3}\\\\.\\\\d{1,3}\\\\.\\\\d{1,3}\") ");
        testPositive("enum-value(../severity) != 5");

    }

    private void testPositive(String exp) {
        assertEquals(of(ValidationHint.autoHint(SKIP_IMPACT_ON_CREATE)),
                m_analyser.getValidationHint(getExpression(exp)));       c_skippedConstraints.add(getExpression(exp).toString());
    }

    private void testNegative(String exp) {
        assertEquals(empty(), m_analyser.getValidationHint(getExpression(exp)));
        c_notSkippedConstraints.add(getExpression(exp).toString());
    }

}
