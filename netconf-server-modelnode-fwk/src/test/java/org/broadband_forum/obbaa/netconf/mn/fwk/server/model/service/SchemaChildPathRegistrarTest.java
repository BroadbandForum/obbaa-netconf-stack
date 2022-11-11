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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrarTest.REF_SCHEMAPATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrarTest.VALIDATION_SCHEMAPATH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrarTest.createQName;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaChildPathRegistrarTest extends AbstractDataStoreValidatorTest {

    protected static final String NAMESPACE = "urn:org:bbf2:pma:extension:test";

    private static final QName SOME_CONTAINER_REF_LEAF = createQName("someContainerRefLeaf");
    private static final SchemaPath SOME_CONTAINER_REF_LEAF_PATH = buildSchemaPath(VALIDATION_SCHEMAPATH, SOME_CONTAINER_REF_LEAF);
    
    @BeforeClass
    public static void initializeOnce()  throws SchemaBuildException {

        SchemaRegistry schemaRegistry =  new SchemaRegistryImpl(TestUtil.getByteSources(getYang()), Collections.emptySet(), Collections.emptyMap(), new NoLockService()) {
            @Override
            public String getMatchingPath(String path) {
                String returnValue = null;
                for (String augmentedPath : getRelativePathCollection()) {
                    if (path.startsWith(augmentedPath)) {
                        if (returnValue != null && augmentedPath.length() > returnValue.length()) {
                            returnValue = augmentedPath;
                        } else if (returnValue == null) {
                            returnValue = augmentedPath;
                        }
                    }
                }
                return returnValue;
            }
        };
        schemaRegistry.registerAppAllowedAugmentedPath("Module1", "/someAbs/validation", mock(SchemaPath.class));
        schemaRegistry.registerAppAllowedAugmentedPath("Module1","/someAbs", mock(SchemaPath.class));
        schemaRegistry.registerAppAllowedAugmentedPath("Module1","/extTest:someAbs", mock(SchemaPath.class));
        m_schemaRegistry = schemaRegistry;

    }

    protected static List<String> getYang() {
        List<String> fileNames = new ArrayList<String>();
        fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test1.yang");
        fileNames.add("/datastorevalidatortest/yangs/extension-test-container.yang");
        return fileNames;

    }

    @Override
    protected void loadDefaultXml() {}

    @Before
    public void setUp() throws ModelNodeInitException, SchemaBuildException {
        super.setUp();
        getModelNode();
    }

    @Test
    public void testChildCrossReferencedPath() throws Exception {
        Expression relativePath = m_schemaRegistry.getRelativePath("/someAbs/validation/leaf1",
                m_schemaRegistry.getDataSchemaNode(REF_SCHEMAPATH));
        assertEquals("../../leaf1", relativePath.toString());
        relativePath = m_schemaRegistry.getRelativePath("/someAbs/validation/someContainer/ref",
                m_schemaRegistry.getDataSchemaNode(SOME_CONTAINER_REF_LEAF_PATH));
        assertEquals("../someContainer/ref", relativePath.toString());
    }

}
