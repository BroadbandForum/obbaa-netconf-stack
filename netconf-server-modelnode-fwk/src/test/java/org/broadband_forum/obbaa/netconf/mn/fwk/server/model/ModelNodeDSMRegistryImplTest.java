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

/**
 * Created by pgorai on 2/3/16.
 */
package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.HashSet;


public class ModelNodeDSMRegistryImplTest {

    ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private SchemaPath m_schemaPath1;
    private ModelNodeDataStoreManager m_dsm1;
    private SchemaPath m_schemaPath2;
    private SchemaPath m_schemaPath3;
    private ModelNodeDataStoreManager m_dsm2;
    public static final String TEST_COMPONENT = "test-component";
    public static final String TEST_COMPONENT1 = "test-component1";


    @Before
    public void setup() {
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();

        m_schemaPath1 = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "play"));
        m_dsm1 = mock(ModelNodeDataStoreManager.class);

        m_schemaPath2 = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2016-01-29",
                "play"));
        m_dsm2 = mock(ModelNodeDataStoreManager.class);

        m_schemaPath3 = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2016-01-29",
                "play2"));
    }

    @Test
    public void undeployTest() {

        m_modelNodeDSMRegistry.register(TEST_COMPONENT, m_schemaPath1, m_dsm1);
        m_modelNodeDSMRegistry.register(TEST_COMPONENT1, m_schemaPath2, m_dsm2);

        assertEquals(m_dsm1, m_modelNodeDSMRegistry.lookupDSM(m_schemaPath1));
        assertEquals(m_dsm2, m_modelNodeDSMRegistry.lookupDSM(m_schemaPath2));

        m_modelNodeDSMRegistry.undeploy(TEST_COMPONENT);
        assertNull(m_modelNodeDSMRegistry.lookupDSM(m_schemaPath1));
        assertNotNull(m_modelNodeDSMRegistry.lookupDSM(m_schemaPath2));

        m_modelNodeDSMRegistry.undeploy("");
        assertNull(m_modelNodeDSMRegistry.lookupDSM(m_schemaPath1));
        assertNotNull(m_modelNodeDSMRegistry.lookupDSM(m_schemaPath2));

        m_modelNodeDSMRegistry.undeploy(TEST_COMPONENT1);
        assertNull(m_modelNodeDSMRegistry.lookupDSM(m_schemaPath2));
    }

    @Test
    public void testGetAllDSMs() {
        m_modelNodeDSMRegistry.register(TEST_COMPONENT, m_schemaPath1, m_dsm1);
        m_modelNodeDSMRegistry.register(TEST_COMPONENT1, m_schemaPath2, m_dsm2);
        m_modelNodeDSMRegistry.register(TEST_COMPONENT1, m_schemaPath3, m_dsm2);
        assertEquals(new HashSet<>(Arrays.asList(m_dsm1, m_dsm2)), m_modelNodeDSMRegistry.getAllDSMs());
    }

}
