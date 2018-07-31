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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;

/**
 * Created by pgorai on 3/2/16.
 */
public class DsmModelNodeHelperDeployerTest {
    public static final String NAMESPACE = "urn:org:bbf:pma:alu-device-plugs";
    public static final String REVISION = "2015-07-14";
    private DsmModelNodeHelperDeployer m_deployer;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDataStoreManager m_modelNodeDSM;

    @Before
    public void setUp() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new NoLockService());
        m_modelNodeDSM = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_deployer = new DsmModelNodeHelperDeployer(m_schemaRegistry, m_modelNodeDSM, m_modelNodeHelperRegistry, null);
    }

    @Test
    public void testDeploy() {
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("jukebox",
                Collections.singletonList((SchemaRegistryVisitor) m_deployer), m_schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, JB_REVISION));
        traverser.traverse();
        assertTrue(m_modelNodeHelperRegistry.getChildContainerHelper(JukeboxConstants.JUKEBOX_SCHEMA_PATH,
                JukeboxConstants.LIBRARY_QNAME) instanceof DsmContainerModelNodeHelper);
        assertTrue(m_modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.LIBRARY_SCHEMA_PATH,
                JukeboxConstants.ARTIST_QNAME) instanceof DsmListModelNodeHelper);
        assertTrue(m_modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.ARTIST_SCHEMA_PATH,
                JukeboxConstants.ALBUM_QNAME) instanceof DsmListModelNodeHelper);
        assertTrue(m_modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.ALBUM_SCHEMA_PATH,
                JukeboxConstants.SONG_QNAME) instanceof DsmListModelNodeHelper);
        assertTrue(m_modelNodeHelperRegistry.getConfigAttributeHelper(JukeboxConstants.SONG_SCHEMA_PATH,
                JukeboxConstants.NAME_QNAME) instanceof DsmConfigAttributeHelper);
        assertTrue(m_modelNodeHelperRegistry.getNaturalKeyHelper(JukeboxConstants.SONG_SCHEMA_PATH,
                JukeboxConstants.NAME_QNAME) instanceof DsmConfigAttributeHelper);
    }

    @Test
    public void testStateContainerNotRegistered() throws SchemaBuildException {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add("/referenceyangs/anvyangs/alu-device-plugs@2015-07-14.yang");
        m_schemaRegistry = new SchemaRegistryImpl(TestUtil.getByteSources(yangFiles), new NoLockService());

        SchemaPath parentSchemaPath = SchemaPath.create(true, QName.create(NAMESPACE, REVISION, "device-plugs-group"));
        SchemaPath devicePlugsSchemaPath = new SchemaPathBuilder().withParent(parentSchemaPath).appendLocalName
                ("device-plugs").build();
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("device-plugs",
                Collections.singletonList((SchemaRegistryVisitor) m_deployer), m_schemaRegistry,
                m_schemaRegistry.getModule("alu-device-plugs", "2015-07-14"));
        traverser.traverse();

        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(devicePlugsSchemaPath, QName.create(NAMESPACE,
                REVISION, "device-plug")));

    }
}
