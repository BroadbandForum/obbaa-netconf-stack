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

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil.getAugmentationSchema;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class AugmentWhenConditionAnalyserTest {
    AugmentWhenConditionAnalyser m_analyser;

    private static SchemaRegistryImpl c_sr;
    private DataSchemaNode cgNode;
    private DataSchemaNode m_posCont1;
    private DataSchemaNode m_ifNode;
    private DataSchemaNode m_augNode2;
    private ReferringNode m_ifTypeRefNode;
    private ReferringNode m_xyRefNode;
    private SchemaPath m_ifTypeSP;
    private SchemaPath m_ifType2SP;
    private DataSchemaNode m_negCont1;
    private DataSchemaNode m_negCont2;
    private SchemaPath m_xySP;
    private ReferringNode m_ifTagRefNode;
    private SchemaPath m_ifsTagSP;
    private DataSchemaNode m_posCont2;
    private DataSchemaNode m_negCont3;
    private ReferringNode m_ifTypeRefNodeWithPredicate;
    private DataSchemaNode m_posCont3;
    private DataSchemaNode m_negCont4;
    private DataSchemaNode m_negCont5;
    private DataSchemaNode m_negCont6;
    private DataSchemaNode m_negCont7;
    private DataSchemaNode m_negCont8;
    private DataSchemaNode m_posCont4;
    private DataSchemaNode m_ifsNode;
    private ReferringNode m_ifRefNode;
    private DataSchemaNode m_posCont5;

    @BeforeClass
    public static void beforeClass() throws SchemaBuildException {
        c_sr = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        c_sr.loadSchemaContext("ut", getYangs(), Collections.emptySet(), Collections.EMPTY_MAP, false);
    }

    @Before
    public void setUp() {
        m_analyser = new AugmentWhenConditionAnalyser();
        m_ifTypeSP = fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface,type");
        m_ifsTagSP = fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces", "(aug:module2?revision=2019-07-28)type2");

        m_xySP = fromString("(aug:module1?revision=2019-07-28)x,y");
        m_ifsNode = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces"));
        m_ifNode = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface"));
        m_posCont1 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)positive-container1"));
        m_posCont2 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)positive-container2"));
        m_posCont3 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)positive-container3"));
        m_posCont4 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces", "(aug:module1?revision=2019-07-28)positive-container4"));
        m_posCont5 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)positive-container5"));
        m_negCont1 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container1"));
        m_negCont2 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container2"));
        m_negCont3 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container3"));
        m_negCont4 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container4"));
        m_negCont5 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container5"));
        m_negCont6 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container6"));
        m_negCont7 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container7"));
        m_negCont8 = c_sr.getDataSchemaNode(fromString("(urn:ietf:params:xml:ns:yang:ietf-interfaces?revision=2014-05-08)interfaces,interface", "(aug:module1?revision=2019-07-28)negative-container8"));

        m_ifTypeRefNode = new ReferringNode(m_ifTypeSP, m_posCont1.getPath(), null);
        m_ifTypeRefNode.setReferenceType(ReferringNode.ReferenceType.WHEN);
        m_ifTypeRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_posCont1).getFirst().getWhenCondition().get().getOriginalString());

        m_ifTypeRefNodeWithPredicate = new ReferringNode(m_ifTypeSP, m_negCont3.getPath(), null);
        m_ifTypeRefNodeWithPredicate.setReferenceType(ReferringNode.ReferenceType.WHEN);
        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont3).getFirst().getWhenCondition().get().getOriginalString());

        m_xyRefNode = new ReferringNode(m_xySP, m_negCont1.getPath(), null);
        m_xyRefNode.setReferenceType(ReferringNode.ReferenceType.WHEN);
        m_xyRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont1).getFirst().getWhenCondition().get().getOriginalString());

        m_ifTagRefNode = new ReferringNode(m_ifsTagSP, m_negCont2.getPath(), null);
        m_ifTagRefNode.setReferenceType(ReferringNode.ReferenceType.WHEN);
        m_ifTagRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont2).getFirst().getWhenCondition().get().getOriginalString());

        m_ifRefNode = new ReferringNode(m_ifsNode.getPath(), m_posCont4.getPath(), null);
        m_ifRefNode.setReferenceType(ReferringNode.ReferenceType.WHEN);
        m_ifRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifsNode, m_posCont4).getFirst().getWhenCondition().get().getOriginalString());

    }

    @Test
    public void testAnalysingPositives() {
        assertTrue(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_posCont1).getSecond(), m_ifTypeRefNode));

        m_ifTypeRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_posCont2).getFirst().getWhenCondition().get().getOriginalString());
        assertTrue(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_posCont2).getSecond(), m_ifTypeRefNode));

        m_ifTypeRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_posCont3).getFirst().getWhenCondition().get().getOriginalString());
        assertTrue(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_posCont3).getSecond(), m_ifTypeRefNode));

        assertTrue(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifsNode, m_posCont4).getSecond(), m_ifRefNode));

        m_ifTypeRefNode.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_posCont5).getFirst().getWhenCondition().get().getOriginalString());
        assertTrue(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_posCont5).getSecond(), m_ifTypeRefNode));

    }

    @Test
    public void testAnalysingNegatives() {
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont1).getSecond(), m_xyRefNode));
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont2).getSecond(), m_ifTagRefNode));
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont3).getSecond(), m_ifTypeRefNodeWithPredicate));

        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont4).getFirst().getWhenCondition().get().getOriginalString());
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont4).getSecond(), m_ifTypeRefNodeWithPredicate));

        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont5).getFirst().getWhenCondition().get().getOriginalString());
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont5).getSecond(), m_ifTypeRefNodeWithPredicate));

        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont6).getFirst().getWhenCondition().get().getOriginalString());
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont6).getSecond(), m_ifTypeRefNodeWithPredicate));

        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont7).getFirst().getWhenCondition().get().getOriginalString());
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont7).getSecond(), m_ifTypeRefNodeWithPredicate));

        m_ifTypeRefNodeWithPredicate.setConstraintXPath(getAugmentationSchema(c_sr, m_ifNode, m_negCont8).getFirst().getWhenCondition().get().getOriginalString());
        assertFalse(m_analyser.areAugmentConditionsUnderAugmentingSubtree(getAugmentationSchema(c_sr, m_ifNode, m_negCont8).getSecond(), m_ifTypeRefNodeWithPredicate));
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

}
