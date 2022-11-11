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

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;

@RunWith(RequestScopeJunitRunner.class)
public class StaticSchemaRegistryDeployerTest extends ModelServiceDeployerTest{

    private StaticSchemaRegistryDeployer m_deployer;

    @Before
    public void setUp() throws ModelServiceDeployerException, SchemaBuildException {
        super.setUp();
        m_deployer = new StaticSchemaRegistryDeployer(m_modelNodeDSMRegistry, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_rpcRequestHandlerRegistry, m_modelNodeHelperDeployer, m_schemaRegistry, new NoLockService());
        m_deployer.setEntityRegistry(m_entityRegistry);
        m_deployer.setRootModelNodeAggregator(m_rootModelNodeAggregator);
        m_deployer.setDataStoreMetadataProvider(m_dataStoreMetaProvider);
        m_deployer.setNetconfServer(m_netconfServer);
    }

    @Test
    public void testDeployDoesNotUpdateSchemaRegistry() throws ModelServiceDeployerException, SchemaBuildException {
        m_deployer.deploy(Arrays.asList(m_modelService));
        verify(m_schemaRegistry, never()).loadSchemaContext(anyString(), anyList(), anySet(), (Map<QName, Set<QName>>) anyMap());
        verify(m_schemaRegistry, never()).buildSchemaContext(anyList());
        verify(m_schemaRegistry, never()).buildSchemaContext(anyList(), anySet(), (Map<QName, Set<QName>>) anyMap());
    }

    @Test
    public void testUnDeployDoesNotUpdateSchemaRegistry() throws SchemaBuildException {
        m_deployer.undeploy(Arrays.asList(m_modelService));
        verify(m_schemaRegistry, never()).unloadSchemaContext(anyString(), eq(null), anyMap());
    }
}
