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

import java.util.Arrays;
import java.util.Collections;

import javax.persistence.LockModeType;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;

import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

public class SingleXmlObjectDSMTest {
    SingleXmlObjectDSM m_dsm;
    @Mock
    private PersistenceManagerUtil m_persistenceMgrUtil;
    @Mock
    private EntityRegistry m_entityRegistry;
    @Mock
    private ModelNodeHelperRegistry m_mnHelperRegistry;
    @Mock
    private SubSystemRegistry m_subsystemRegistry;
    @Mock
    private ModelNodeDSMRegistry m_mnDSMRegistry;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    @Mock
    private ModelNodeKey m_mnKey;
    @Mock
    private ModelNodeId m_parentId;
    @Mock
    private Object m_entity;

    @Before
    public void setUp() throws AnnotationAnalysisException {
        MockitoAnnotations.initMocks(this);
        m_dsm = new SingleXmlObjectDSM(m_entity, m_persistenceMgrUtil, m_entityRegistry, m_schemaRegistry,
                m_mnHelperRegistry, m_subsystemRegistry, m_mnDSMRegistry);
    }

    @Test
    public void testGetParentEntityReturnsSingleEntity() {
        assertEquals(m_entity, m_dsm.getParentEntity(getClass(), m_mnKey, m_parentId, LockModeType.PESSIMISTIC_WRITE));
    }

    @Test
    public void testGetParentEntitiesReturnsSingleEntity() {
        assertEquals(Arrays.asList(m_entity), m_dsm.getParentEntities(getClass(), Collections.emptyMap(), m_parentId));
    }

}
