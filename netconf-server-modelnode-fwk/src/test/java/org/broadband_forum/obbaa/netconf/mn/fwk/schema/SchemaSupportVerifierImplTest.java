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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SchemaSupportVerifierImplTest {
    
    private SchemaSupportVerifier m_verifier = new SchemaSupportVerifierImpl();
    private static final YangTextSchemaSource TEST_YANG = YangParserUtil.getYangSource(SchemaSupportVerifierImplTest.class.getResource("/schemasupportverifiertest/test.yang"));
    private static Logger LOGGER = LoggerFactory.getLogger(SchemaSupportVerifierImplTest.class);

    @Test
    @Ignore
    public void testVerify() {
        SchemaContext sc;
        try {
            sc = YangParserUtil.parseSchemaSources("", Arrays.asList(TEST_YANG), null, null);
        } catch (Exception e) {
            LOGGER.error("Error parsing schema sources", e);
            throw e;
        }
        
        Set<String> expectedErrors = new HashSet<String>();
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)last-function: XPath core function 'last' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)xpath-syntax-error: Invalid XPath: 'current( = 3'. Syntax error after: 'c'");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)position-function: XPath core function 'position' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)id-leaf: XPath core function 'id' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)choice-with-lang: XPath core function 'lang' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)case-with-name: XPath core function 'name' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)double-error: XPath core function 'name' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)double-error: XPath core function 'position' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)deref-function: XPath extension function 'deref' is not supported yet. Contact Test Company to plan support for this function");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)unknown-function: Unknown XPath function 'unknown'");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-on-path-expression-predicate: XPath core function 'name' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-rpc-output-leaf: XPath core function 'lang' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-rpc-input-leaf: XPath core function 'id' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-action-input-leaf: XPath core function 'id' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-action-output-leaf: XPath core function 'lang' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-notification-leaf: XPath core function 'id' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)error-in-top-notification-leaf: XPath core function 'id' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)ancestor-axis: XPath axis 'ancestor' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)ancestor-or-self-axis: XPath axis 'ancestor-or-self' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)container-in-list: XPath axis 'descendant' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)container-in-list: XPath axis 'descendant-or-self' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)top-list: XPath axis 'descendant-or-self' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)attribute-abbrev-axis: XPath axis 'attribute' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)attribute-axis: XPath axis 'attribute' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)following-preceding-axis: XPath axis 'following' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)following-preceding-axis: XPath axis 'following-sibling' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)following-preceding-axis: XPath axis 'preceding-sibling' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)following-preceding-axis: XPath axis 'preceding' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)namespace-axis: XPath axis 'namespace' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)wildcard-leaf: Wildcard * is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)pi-leaf: XPath node type test 'processing-instruction()' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)pi2-leaf: XPath processing instruction test is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)comment-leaf: XPath node type test 'comment()' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)text-leaf: XPath node type test 'text()' is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)top-container: Indexing in an XPath step is not supported");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)container-in-list: XPath core operation '|' is not supported");
        expectedErrors.add("Leaf List (urn:test?revision=2019-01-15)leaflist-with-default has one or more default values. Default values for leaf-list are not supported yet. Contact Test Company to plan support for this function");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)not-without-arg: Invalid JXPath syntax: Incorrect number of arguments for the function 'not'");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)false-with-arg: Invalid JXPath syntax: Arguments not allowed for the function 'false'");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)true-with-arg: Invalid JXPath syntax: Arguments not allowed for the function 'true'");
        expectedErrors.add("Problem in XPath in (urn:test?revision=2019-01-15)wrong-substring-index: String indexing in XPath function substring starts from 1, not 0");

        try {
            RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
                @Override
                protected Void execute() throws RequestScopeExecutionException {
                    m_verifier.verify(sc);
                    return null;
                }
            });
            fail("The verification should fail");
        } catch (Exception e) {
            String message = e.getMessage();
            String[] errors = message.split("\n");
            Set<String> actualErrors = Sets.newHashSet(errors);
            
            Set<String> missingErrors = Sets.difference(expectedErrors, actualErrors);
            Set<String> wrongErrors = Sets.difference(actualErrors, expectedErrors);
            assertTrue("Missing errors: " + missingErrors + ", wrong errors: " + wrongErrors, missingErrors.isEmpty() && wrongErrors.isEmpty()); 
        }
    }
}
