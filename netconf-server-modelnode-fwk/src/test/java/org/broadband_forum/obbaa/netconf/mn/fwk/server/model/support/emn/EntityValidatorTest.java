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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses.HomeAddress;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses.TelephoneNumber;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class EntityValidatorTest {

    public static final String JUKEBOX_COMPONENT_ID = "Jukebox";
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(JUKEBOX_COMPONENT_ID, TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
    }

    @Test
    public void testJukeboxEntityClass() throws AnnotationAnalysisException {

        List<Class> rootClasses = new ArrayList<>();
        rootClasses.add(Jukebox.class);
        Map<Class,List<String>> result = EntityValidator.validateRootClasses(m_schemaRegistry, rootClasses);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testJukeboxWithLibrarySubtreeEntityClass(){
        List<Class> rootClasses = new ArrayList<>();
        rootClasses.add(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree.Jukebox.class);
        Map<Class,List<String>> result = EntityValidator.validateRootClasses(m_schemaRegistry, rootClasses);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testJukeboxWithArtistSubtreeEntityClass(){
        List<Class> rootClasses = new ArrayList<>();
        rootClasses.add(org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree.Jukebox.class);
        Map<Class,List<String>> result = EntityValidator.validateRootClasses(m_schemaRegistry, rootClasses);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testErrorScenarios() throws SchemaBuildException {
        List<Class> rootClasses = new ArrayList<>();
        rootClasses.add(HomeAddress.class);
        Map<Class,List<String>> result = EntityValidator.validateRootClasses(m_schemaRegistry, rootClasses);
        assertEquals(1, result.size());
        assertEquals(HomeAddress.class, result.keySet().iterator().next());

        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/yangs/addresses@2015-12-08.yang");

        m_schemaRegistry.loadSchemaContext("HomeAddress", TestUtil.getByteSources(yangFiles), Collections.emptySet(), Collections.emptyMap());

        result = EntityValidator.validateRootClasses(m_schemaRegistry, rootClasses);
        assertEquals(2, result.size());

        List<String> errors = result.get(HomeAddress.class);
        assertEquals(1, errors.size());
        assertEquals("Invalid attribute found OR name component not specified- address" , errors.get(0));

        errors = result.get(TelephoneNumber.class);
        assertEquals(2, errors.size());
        assertTrue(errors.contains("Invalid attribute found OR name component not specified- type"));
        assertTrue(errors.contains("Invalid attribute found OR name component not specified- number"));
    }
}
