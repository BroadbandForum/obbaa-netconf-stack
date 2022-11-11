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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.Collections;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;

public class ThreadLocalDSMRegistryTest {
    ThreadLocalDSMRegistry m_dsm;
    @Mock
    private ModelNodeDataStoreManager m_dsm1;

    @Before
    public void setUp() throws AnnotationAnalysisException {
        MockitoAnnotations.initMocks(this);
        m_dsm = new ThreadLocalDSMRegistry();
        ThreadLocalDSMRegistry.setDsm(m_dsm1);
    }

    @After
    public void tearDown(){
        ThreadLocalDSMRegistry.clearDsm();
    }

    @Test
    public void testGetAllDSMs(){
        assertEquals(Collections.singleton(m_dsm1), m_dsm.getAllDSMs());
    }


    @Test
    public void testLookupDSM(){
        assertEquals(m_dsm1, m_dsm.lookupDSM(mock(SchemaPath.class)));
    }

}
