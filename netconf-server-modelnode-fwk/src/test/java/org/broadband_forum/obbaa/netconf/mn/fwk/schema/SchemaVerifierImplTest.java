/*
 * Copyright 2021 Broadband Forum
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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import com.google.common.collect.Sets;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaVerifierImplTest extends AbstractDataStoreValidatorTest {
    private static final String UNIQUE_YANG_FILE = "/datastorevalidatortest/yangs/bad-unique-constraints-main.yang";
    private static final String UNIQUE_YANG_DEV_FILE = "/datastorevalidatortest/yangs/bad-unique-constraints-dev.yang";
    private Map<QName, Set<QName>> m_supportedDeviations = new HashMap<>();

    @Before
    public void setup() throws Exception {
        YangTextSchemaSource unique_yang = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(UNIQUE_YANG_FILE));
        YangTextSchemaSource unique_yang_dev = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource(UNIQUE_YANG_DEV_FILE));
        QName moduleQName = QName.create("urn:org:bbf2:pma:bad-unique-constraints-main", "2015-12-14", "bad-unique-constraints-main");
        QName deviationQName = QName.create("urn:org:bbf2:pma:bad-unique-constraints-dev", "2015-12-14", "bad-unique-constraints-dev");
        Set<QName> deviations = new HashSet<>();
        deviations.add(deviationQName);
        m_supportedDeviations.put(moduleQName, deviations);
        m_schemaRegistry = new SchemaRegistryImpl(Arrays.asList(unique_yang, unique_yang_dev), Collections.emptySet(), m_supportedDeviations, new NoLockService());
        m_modelNodeDsm = spy(new InMemoryDSM(m_schemaRegistry));
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    }

    @Test
    public void testUniqueConstraints() {
        Set<String> expectedErrors = getExpectedErrorList();
        try {
            YangUtils.deployInMemoryHelpers(Arrays.asList(UNIQUE_YANG_FILE, UNIQUE_YANG_DEV_FILE), new LocalSubSystem(),
                    m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm, Collections.emptySet(), m_supportedDeviations, true);
            fail("Exception expected");
        } catch (Exception e) {
            String message = e.getMessage();
            String[] errors = message.split("\n");
            Set<String> detectedErrors = Sets.newHashSet(errors);
            Set<String> unexpectedErrors = Sets.difference(detectedErrors, expectedErrors);
            Set<String> missingErrors = Sets.difference(expectedErrors, detectedErrors);
            assertTrue("Unexpected errors: " + unexpectedErrors + ", missing errors: " + missingErrors, unexpectedErrors.isEmpty() && missingErrors.isEmpty());
        }
    }

    @Test
    public void testSchemaVerifyWithoutUniqueConstraints() {
        SchemaVerifierImpl schemaVerifier = spy(new SchemaVerifierImpl());
        when(schemaVerifier.isSchemaVerificationEnabled()).thenReturn(false);
        try {
            schemaVerifier.verify(m_schemaRegistry);
        } catch (Exception e) {
            fail("Exception not expected because we are not checking unique constraints");
        }
    }

    @Test
    public void testSchemaVerifyWithUniqueConstraints() {
        Set<String> expectedErrors = getExpectedErrorList();
        SchemaVerifierImpl schemaVerifier = new SchemaVerifierImpl();
        schemaVerifier.setIsSchemaVerificationEnabledForTest(true);
        try {
            assertTrue(schemaVerifier.isSchemaVerificationEnabled());
            schemaVerifier.verify(m_schemaRegistry);
            fail("Exception expected");
        } catch (Exception e) {
            String message = e.getMessage();
            String[] errors = message.split("\n");
            Set<String> detectedErrors = Sets.newHashSet(errors);
            Set<String> unexpectedErrors = Sets.difference(detectedErrors, expectedErrors);
            Set<String> missingErrors = Sets.difference(expectedErrors, detectedErrors);
            assertTrue("Unexpected errors: " + unexpectedErrors + ", missing errors: " + missingErrors, unexpectedErrors.isEmpty() && missingErrors.isEmpty());

        }
    }

    @Test
    public void testIsSchemaVerificationDisableSkipsVerify() throws SchemaBuildException {
        SchemaVerifierImpl schemaVerifier = spy(new SchemaVerifierImpl());
        schemaVerifier.setIsSchemaVerificationEnabledForTest(false);
        schemaVerifier.verify(m_schemaRegistry);

        verify(schemaVerifier, never()).checkUniqueConstraints(eq(m_schemaRegistry),anySet());
        verify(schemaVerifier, never()).checkCircularDependencyforLeafrefs(eq(m_schemaRegistry),anySet());
    }

    private Set<String> getExpectedErrorList() {
        Set<String> errorList = new HashSet<String>();
        errorList.add("Nodes in unique constraint of 'test-list' don't exist (maybe wrong namespace?): Relative{path=[(urn:org:bbf2:pma:bad-unique-constraints-dev?revision=2015-12-14)thirdleaf]}");
        errorList.add("Nodes in unique constraint of 'test-list' don't exist (maybe wrong namespace?): Relative{path=[(urn:org:bbf2:pma:bad-unique-constraints-dev?revision=2015-12-14)ipv4, (urn:org:bbf2:pma:bad-unique-constraints-dev?revision=2015-12-14)ip-address]}");
        return errorList;
    }
}
