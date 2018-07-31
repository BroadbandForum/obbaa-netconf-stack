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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

/**
 * Runs each test 'n' number of times defined by times() method. Currently n = 100
 */

@RunWith(Parameterized.class)
public class SchemaRegistryMultiThreadTest {

    private static final Logger LOGGER = Logger.getLogger(SchemaRegistryMultiThreadTest.class);

    private final SchemaPath constraintSchemaPath1 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest
            .JB_NS).appendLocalName("jukebox1").build();

    private final SchemaPath constraintSchemaPath2 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest
            .JB_NS).appendLocalName("jukebox2").build();

    private final SchemaPath nodeSchemaPath1 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library1").build();

    private final SchemaPath nodeSchemaPath2 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library2").build();

    private final SchemaPath nodeSchemaPath3 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library3").build();

    private final SchemaPath nodeSchemaPath4 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library4").build();

    private final SchemaPath nodeSchemaPath5 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library5").build();

    private final SchemaPath nodeSchemaPath6 = new SchemaPathBuilder().withNamespace(SchemaRegistryImplTest.JB_NS)
            .appendLocalName("jukebox")
            .appendLocalName("library6").build();

    private SchemaRegistry m_schemaRegistry;
    private List<Exception> m_exceptionList;
    private static int m_count = 0;

    @Parameterized.Parameters
    public static List<Object[]> times() {
        return Arrays.asList(new Object[100][0]);
    }

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), new NoLockService());
        m_exceptionList = Collections.synchronizedList(new ArrayList<Exception>());
    }

    @Test
    public void testLoadUnloadImpactedNodes() {
        m_count++;

        try {
            List<Thread> threadList = new ArrayList<Thread>();
            for (int i = 0; i < 100; i++) {
                TestClass1 thread1 = new TestClass1();
                TestClass2 thread2 = new TestClass2();
                thread1.start();
                thread2.start();
                threadList.add(thread1);
                threadList.add(thread2);
            }

            for (Thread thread : threadList) {
                thread.join();
            }

            for (Exception exception : m_exceptionList) {
                LOGGER.error(exception);
            }
            assertTrue(m_exceptionList.isEmpty());
            assertTrue(m_schemaRegistry.getReferencedNodesForSchemaPaths(nodeSchemaPath1).size() == 2);
            assertTrue(m_schemaRegistry.getReferencedNodesForSchemaPaths(nodeSchemaPath1).keySet().contains
                    (constraintSchemaPath1));
            assertTrue(m_schemaRegistry.getReferencedNodesForSchemaPaths(nodeSchemaPath1).keySet().contains
                    (constraintSchemaPath2));

        } catch (Exception e) {
            LOGGER.error("TC failed during " + m_count + "th run", e);
            fail("Yet another UT failure: " + e.getMessage() + "\nTC failed during " + m_count + "th run");
        }
    }

    private class TestClass1 extends Thread {
        @Override
        public void run() {
            try {
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath4, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath5, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath6, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath1, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath2, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath3, null);
            } catch (Exception e) {
                m_exceptionList.add(e);
            }
        }
    }

    private class TestClass2 extends Thread {
        @Override
        public void run() {
            try {
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath1, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath2, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath2,
                        nodeSchemaPath3, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath4, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath5, null);
                m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath1,
                        nodeSchemaPath6, null);
            } catch (Exception e) {
                m_exceptionList.add(e);
            }
        }
    }
}
