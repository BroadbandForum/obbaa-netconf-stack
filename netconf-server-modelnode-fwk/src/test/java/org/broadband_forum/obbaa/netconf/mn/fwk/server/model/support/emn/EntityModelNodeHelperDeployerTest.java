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

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_MODULE_NAME;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by keshava on 12/8/15.
 */
public class EntityModelNodeHelperDeployerTest {

    private static final String COMPONENT_ID_1 = "component_Id_1";

    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    private SchemaRegistry m_schemaRegistry;

    private ModelNodeDataStoreManager m_aggregatedDSM;

    private EntityRegistry m_entityRegistry;

    private EntityModelNodeHelperDeployer m_entityModelNodeHelperDeployer;

    @Before
    public void setUp() throws SchemaBuildException {
        // prepare entityModelNodeHelperDeployer
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        m_schemaRegistry = spy(new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new NoLockService()));
        m_aggregatedDSM = mock(ModelNodeDataStoreManager.class);
        m_entityRegistry = mock(EntityRegistry.class);
        m_entityModelNodeHelperDeployer = new EntityModelNodeHelperDeployer(m_modelNodeHelperRegistry,
                m_schemaRegistry, m_aggregatedDSM,
                m_entityRegistry, null);
    }

    @Test
    public void testDeploy() throws AnnotationAnalysisException {
        AbstractApplicationContext context = new ClassPathXmlApplicationContext(
                "/entitymodelnodehelperdeployertest/test-applicationContext.xml");
        SchemaRegistry schemaRegistry = (SchemaRegistry) context.getBean("schemaRegistry");
        ModelNodeHelperRegistry modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        EntityRegistry entityRegistry = (EntityRegistry) context.getBean("entityRegistry");
        EntityModelNodeHelperDeployer entityModelNodeHelperDeployer = new EntityModelNodeHelperDeployer
                (modelNodeHelperRegistry,
                schemaRegistry, m_aggregatedDSM, entityRegistry, null);

        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("jukebox",
                Collections.singletonList((SchemaRegistryVisitor) entityModelNodeHelperDeployer), schemaRegistry,
                m_schemaRegistry.getModule(JUKEBOX_MODULE_NAME, JB_REVISION));
        traverser.traverse();
        assertTrue(modelNodeHelperRegistry.getChildContainerHelper(JukeboxConstants.JUKEBOX_SCHEMA_PATH,
                JukeboxConstants.LIBRARY_QNAME)
                instanceof XmlContainerModelNodeHelper);
        assertTrue(modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.LIBRARY_SCHEMA_PATH, JukeboxConstants
                .ARTIST_QNAME)
                instanceof XmlListModelNodeHelper);
        assertTrue(modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.ARTIST_SCHEMA_PATH, JukeboxConstants
                .ALBUM_QNAME)
                instanceof XmlListModelNodeHelper);
        assertTrue(modelNodeHelperRegistry.getChildListHelper(JukeboxConstants.ALBUM_SCHEMA_PATH, JukeboxConstants
                .SONG_QNAME)
                instanceof XmlListModelNodeHelper);
        assertTrue(modelNodeHelperRegistry.getConfigAttributeHelper(JukeboxConstants.SONG_SCHEMA_PATH,
                JukeboxConstants.NAME_QNAME)
                instanceof DsmConfigAttributeHelper);
        assertTrue(modelNodeHelperRegistry.getNaturalKeyHelper(JukeboxConstants.SONG_SCHEMA_PATH, JukeboxConstants
                .NAME_QNAME)
                instanceof DsmConfigAttributeHelper);
        assertTrue(modelNodeHelperRegistry.getConfigLeafListHelper(JukeboxConstants.SONG_SCHEMA_PATH,
                JukeboxConstants.SINGER_QNAME)
                instanceof XmlChildLeafListHelper);

    }

    @Test
    public void testVisitLeafNodeWhenConfig() {
        // prepare schemaRegistery with ListSchemaNode
        ListSchemaNode albumSchemaNode = mock(ListSchemaNode.class);
        when(albumSchemaNode.getKeyDefinition()).thenReturn(Collections.<QName>emptyList());
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.ALBUM_SCHEMA_PATH)).thenReturn(albumSchemaNode);

        // prepare config leaf node
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(leafSchemaNode.getQName()).thenReturn(JukeboxConstants.NAME_QNAME);
        when(leafSchemaNode.isConfiguration()).thenReturn(true);

        // test visitLeafNode
        m_entityModelNodeHelperDeployer.visitLeafNode(COMPONENT_ID_1, JukeboxConstants.ALBUM_SCHEMA_PATH,
                leafSchemaNode);

        // verify config leaf is registered
        ConfigAttributeHelper helper = new DsmConfigAttributeHelper(m_aggregatedDSM, m_schemaRegistry, leafSchemaNode,
                JukeboxConstants.NAME_QNAME);
        verify(m_modelNodeHelperRegistry, times(1)).registerConfigAttributeHelper(COMPONENT_ID_1, JukeboxConstants
                        .ALBUM_SCHEMA_PATH,
                JukeboxConstants.NAME_QNAME, helper);
    }

    @Test
    public void testVisitLeafNodeWhenNonConfig() {
        // prepare schemaRegistery with ListSchemaNode
        ListSchemaNode albumSchemaNode = mock(ListSchemaNode.class);
        when(albumSchemaNode.getKeyDefinition()).thenReturn(Collections.<QName>emptyList());
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.LIBRARY_SCHEMA_PATH)).thenReturn(albumSchemaNode);

        // prepare non config leaf node
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(leafSchemaNode.getQName()).thenReturn(JukeboxConstants.ARTIST_COUNT_QNAME);
        when(leafSchemaNode.isConfiguration()).thenReturn(false);
        // test visitLeafNode
        m_entityModelNodeHelperDeployer.visitLeafNode(COMPONENT_ID_1, JukeboxConstants.LIBRARY_SCHEMA_PATH,
                leafSchemaNode);

        // verify non config is not registered
        verify(m_modelNodeHelperRegistry, times(0)).registerConfigAttributeHelper(any(String.class), any(SchemaPath
                        .class), any(QName.class),
                any(ConfigAttributeHelper.class));
    }

    @Test
    public void testVisitLeafListNode() {
        // prepare schemaRegistry with ListSchemaNode
        ListSchemaNode songSchemaNode = mock(ListSchemaNode.class);
        when(songSchemaNode.getKeyDefinition()).thenReturn(Collections.<QName>emptyList());
        when(m_schemaRegistry.getDataSchemaNode(JukeboxConstants.SONG_SCHEMA_PATH)).thenReturn(songSchemaNode);

        // prepare config leaf-list node
        LeafListSchemaNode singerSchemaNode = mock(LeafListSchemaNode.class);
        when(singerSchemaNode.getQName()).thenReturn(JukeboxConstants.SINGER_QNAME);
        when(singerSchemaNode.isConfiguration()).thenReturn(true);

        // test visit leaf list schemaNode
        m_entityModelNodeHelperDeployer.visitLeafListNode(COMPONENT_ID_1, JukeboxConstants.SONG_SCHEMA_PATH,
                singerSchemaNode);

        // verify config leaf-list is registered
        verify(m_modelNodeHelperRegistry, times(1)).registerConfigLeafListHelper(eq(COMPONENT_ID_1), eq
                        (JukeboxConstants.SONG_SCHEMA_PATH),
                eq(JukeboxConstants.SINGER_QNAME), any(ChildLeafListHelper.class));

        LeafListSchemaNode languageSchemaNode = mock(LeafListSchemaNode.class);
        QName languageQname = QName.create(JukeboxConstants.JB_NS, JB_REVISION, "language");
        when(languageSchemaNode.getQName()).thenReturn(languageQname);
        when(languageSchemaNode.isConfiguration()).thenReturn(false);

        // test visit state leaf list schemaNode
        m_entityModelNodeHelperDeployer.visitLeafListNode(COMPONENT_ID_1, JukeboxConstants.SONG_SCHEMA_PATH,
                languageSchemaNode);

        // verify state leaf-list is not registered
        verify(m_modelNodeHelperRegistry, never()).registerConfigLeafListHelper(eq(COMPONENT_ID_1), eq
                        (JukeboxConstants.SONG_SCHEMA_PATH),
                eq(languageQname), any(ChildLeafListHelper.class));
    }

}
