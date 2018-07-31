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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by pgorai on 1/27/16.
 */
public class SubSystemRegistryTest {

    SubSystemRegistry m_subSystemRegistry;
    private SchemaPath m_schemaPath;
    private SubSystem m_subSystem;
    private SchemaPath m_schemaPath1;
    private SubSystem m_subSystem1;
    private SchemaPath m_schemaPath2;
    private SubSystem m_subSystem2;
    public static final String TEST_COMPONENT = "test-component";
    public static final String TEST_COMPONENT1 = "test-component1";

    @Before
    public void setup() {
        m_subSystemRegistry = new SubSystemRegistryImpl();

        m_subSystem = mock(SubSystem.class);
        m_schemaPath = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2014-07-03",
                "play"));

        m_schemaPath1 = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2014-07-03"));
        m_subSystem1 = mock(SubSystem.class);

        m_schemaPath2 = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2016-01-29",
                "play"));
        m_subSystem2 = mock(SubSystem.class);
    }

    @Test
    public void undeployTest() {

        m_subSystemRegistry.register(TEST_COMPONENT, m_schemaPath, m_subSystem);
        m_subSystemRegistry.register(TEST_COMPONENT, m_schemaPath1, m_subSystem1);
        m_subSystemRegistry.register(TEST_COMPONENT1, m_schemaPath2, m_subSystem2);

        assertEquals(m_subSystem, m_subSystemRegistry.lookupSubsystem(m_schemaPath));
        assertEquals(m_subSystem1, m_subSystemRegistry.lookupSubsystem(m_schemaPath1));
        assertEquals(m_subSystem2, m_subSystemRegistry.lookupSubsystem(m_schemaPath2));

        m_subSystemRegistry.undeploy(TEST_COMPONENT);
        assertEquals(NoopSubSystem.c_instance, m_subSystemRegistry.lookupSubsystem(m_schemaPath));
        assertEquals(NoopSubSystem.c_instance, m_subSystemRegistry.lookupSubsystem(m_schemaPath1));
        assertNotNull(m_subSystemRegistry.lookupSubsystem(m_schemaPath2));

        m_subSystemRegistry.undeploy(TEST_COMPONENT1);
        assertEquals(NoopSubSystem.c_instance, m_subSystemRegistry.lookupSubsystem(m_schemaPath2));

        //try undeploy without registering any subsystems
        m_subSystemRegistry.undeploy(TEST_COMPONENT1);
    }

}
