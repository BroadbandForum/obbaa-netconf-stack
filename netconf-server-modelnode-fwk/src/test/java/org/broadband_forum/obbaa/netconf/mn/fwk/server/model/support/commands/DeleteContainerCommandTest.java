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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADMIN_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class DeleteContainerCommandTest extends AbstractCommandTest {

    private static final ModelNodeId TEST_ALBUM_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist" +
            "/name=Lenny/container=album/name=test-album", JB_NS);
    private static final ModelNodeId ADMIN_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist" +
            "/name=Lenny/container=album/name=test-album/container=admin", JB_NS);
    private static final ModelNodeId CATALOGUE_NUMBER_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist" +
            "/name=Lenny/container=album/name=test-album/container=admin/container=catalogue-number", JB_NS);
    private static final ModelNodeId LABEL_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny" +
            "/container=album/name=test-album/container=admin/container=label", JB_NS);

    private DeleteContainerCommand m_deleteContainerCommand;

    @Mock
    public ChildContainerHelper m_childContainerHelper;

    @Before
    public void setUp() throws Exception {
        m_deleteContainerCommand = new DeleteContainerCommand().addDeleteInfo(m_childContainerHelper, m_jukeboxNode, m_libraryNode,
                EditChangeSource.system);
        when(m_childContainerHelper.getSchemaNode()).thenReturn((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(LIBRARY_SCHEMA_PATH));
    }

    @Test
    public void testAppendChangesAddsDeleteInfo() {
        WritableChangeTreeNode jukeboxTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, JUKEBOX_MNID, m_jukeboxDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_deleteContainerCommand.appendChange(jukeboxTN, false);

        Map<ModelNodeId, ChangeTreeNode> jukeboxTNChildren = jukeboxTN.getChildren();
        assertEquals(1, jukeboxTNChildren.size());
        assertEquals("library[/jukebox/library] -> delete", jukeboxTN.getNodesOfType(LIBRARY_SCHEMA_PATH).iterator().next().print().trim());
        ChangeTreeNode libraryCTN = jukeboxTNChildren.get(LIBRARY_MNID);
        assertFalse(libraryCTN.isImplied());
        assertEquals(EditChangeSource.system, libraryCTN.getChangeSource());
    }

    @Test
    public void testAppendChangesAddsPreviousLeafValues() {
        ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        ModelNode albumModelNode = mock(ModelNode.class);
        ModelNode adminModelNode = mock(ModelNode.class);
        when(albumModelNode.getModelNodeId()).thenReturn(TEST_ALBUM_MNID);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getQName()).thenReturn(ALBUM_QNAME);

        ChildContainerHelper adminHelper = mock(ChildContainerHelper.class);
        when(adminHelper.getSchemaNode()).thenReturn((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(ADMIN_SCHEMA_PATH));
        when(adminModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);

        ConfigAttributeHelper labelHelper = mock(ConfigAttributeHelper.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ADMIN_SCHEMA_PATH, LABEL_QNAME)).thenReturn(labelHelper);
        LeafSchemaNode labelSchemaNode = mock(LeafSchemaNode.class);
        when(labelHelper.getLeafSchemaNode()).thenReturn(labelSchemaNode);
        when(labelSchemaNode.getQName()).thenReturn(LABEL_QNAME);

        ConfigAttributeHelper catalogueNoHelper = mock(ConfigAttributeHelper.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ADMIN_SCHEMA_PATH, CATALOGUE_NUMBER_QNAME)).thenReturn(catalogueNoHelper);
        LeafSchemaNode catalogueNoSchemaNode = mock(LeafSchemaNode.class);
        when(catalogueNoHelper.getLeafSchemaNode()).thenReturn(catalogueNoSchemaNode);
        when(catalogueNoSchemaNode.getQName()).thenReturn(CATALOGUE_NUMBER_QNAME);

        when(adminModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(adminModelNode.getModelNodeSchemaPath()).thenReturn(ADMIN_SCHEMA_PATH);
        m_deleteContainerCommand = new DeleteContainerCommand().addDeleteInfo(adminHelper, albumModelNode, adminModelNode, EditChangeSource.system);

        Map<QName, ConfigLeafAttribute> previousLeafValues = new LinkedHashMap<>();
        previousLeafValues.put(LABEL_QNAME, new GenericConfigAttribute(LABEL, JB_NS, "test-label"));
        previousLeafValues.put(CATALOGUE_NUMBER_QNAME, new GenericConfigAttribute(CATALOGUE_NUMBER, JB_NS, "test-catalogue-number"));
        when(adminModelNode.getAttributes()).thenReturn(previousLeafValues);

        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, TEST_ALBUM_MNID,
                m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap,
                m_nodesOfTypeWithinSchemaMount);

        m_deleteContainerCommand.appendChange(albumCTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumCTNChildren = albumCTN.getChildren();
        assertEquals(1, albumCTNChildren.size());
        ChangeTreeNode adminCTN = albumCTN.getNodesOfType(ADMIN_SCHEMA_PATH).iterator().next();
        assertEquals("admin[/jukebox/library/artist[name='Lenny']/album[name='test-album']/admin] -> delete\n" +
                " label -> delete { previousVal = 'test-label', currentVal = 'null' }\n" +
                " catalogue-number -> delete { previousVal = 'test-catalogue-number', currentVal = 'null' }", adminCTN.print().trim());
        assertFalse(albumCTNChildren.get(ADMIN_MNID).isImplied());
        assertEquals(EditChangeSource.system, adminCTN.getChangeSource());
        assertEquals(EditChangeSource.system, adminCTN.getChildren().get(LABEL_MNID).getChangeSource());
        assertEquals(EditChangeSource.system, adminCTN.getChildren().get(CATALOGUE_NUMBER_MNID).getChangeSource());
    }

    @Test
    public void testAppendChangesDoesNotAddChildNodesWhenCurrentNodeIsNotUnderSchemaMount() {
        ModelNode jukeboxModelNode = mock(ModelNode.class);
        ModelNode libraryModelNode = mock(ModelNode.class);

        when(jukeboxModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(jukeboxModelNode.getModelNodeId()).thenReturn(JUKEBOX_MNID);
        when(libraryModelNode.getAttributes()).thenReturn(Collections.EMPTY_MAP);

        ChildContainerHelper libraryHelper = mock(ChildContainerHelper.class);
        when(libraryHelper.getSchemaNode()).thenReturn((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(LIBRARY_SCHEMA_PATH));
        m_deleteContainerCommand = new DeleteContainerCommand().addDeleteInfo(libraryHelper, jukeboxModelNode, libraryModelNode, EditChangeSource.system);

        WritableChangeTreeNode jukeboxCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, JUKEBOX_MNID,
                m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap,
                m_nodesOfTypeWithinSchemaMount);
        jukeboxCTN.setMountPoint(false);


        m_deleteContainerCommand.appendChange(jukeboxCTN, false);

        Map<ModelNodeId, ChangeTreeNode> jukeboxCTNChildren = jukeboxCTN.getChildren();
        assertEquals(1, jukeboxCTNChildren.size());
        ChangeTreeNode libraryCTN = jukeboxCTN.getNodesOfType(LIBRARY_SCHEMA_PATH).iterator().next();
        assertEquals("library[/jukebox/library] -> delete", libraryCTN.print().trim());
        assertFalse(jukeboxCTNChildren.get(LIBRARY_MNID).isMountPoint());
    }

    @Test
    public void testAppendChangesAddsChildNodesWhenCurrentNodeIsUnderSchemaMount() {
        ModelNode jukeboxModelNode = mock(ModelNode.class);
        XmlModelNodeImpl libraryModelNode = mock(XmlModelNodeImpl.class);
        XmlModelNodeImpl artistModelNode = mock(XmlModelNodeImpl.class);

        when(artistModelNode.getKey()).thenReturn(LENNY_MNID);
        IndexedList artistList = new IndexedList<ModelNodeId, XmlModelNodeImpl>();
        artistList.add(artistModelNode);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> artistMap = new HashMap<>();
        artistMap.put(ARTIST_QNAME, artistList);

        when(jukeboxModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(jukeboxModelNode.getModelNodeId()).thenReturn(JUKEBOX_MNID);
        when(libraryModelNode.getAttributes()).thenReturn(Collections.EMPTY_MAP);
        when(libraryModelNode.getChildren()).thenReturn(artistMap);

        ChildContainerHelper libraryHelper = mock(ChildContainerHelper.class);
        when(libraryHelper.getSchemaNode()).thenReturn((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(LIBRARY_SCHEMA_PATH));

        ConfigAttributeHelper nameHelper = mock(ConfigAttributeHelper.class);
        SchemaPath nameSP = new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH).appendLocalName(NAME).build();
        when(nameHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(nameSP));

        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_schemaRegistry
                .getDataSchemaNode(ARTIST_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setChangeType(ChangeTreeNode.ChangeType.delete);

        ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ARTIST_SCHEMA_PATH, NAME_QNAME)).thenReturn(nameHelper);

        Map<QName, ConfigLeafAttribute> artistAttributes = new HashMap<>();
        artistAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "lenny"));
        when(artistModelNode.buildCtnForDelete(any(WritableChangeTreeNode.class))).thenReturn(artistCTN);
        when(artistModelNode.getAttributes()).thenReturn(artistAttributes);
        when(artistModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(artistModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);
        when(artistModelNode.getModelNodeSchemaPath()).thenReturn(ARTIST_SCHEMA_PATH);

        WritableChangeTreeNode jukeboxCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, JUKEBOX_MNID,
                m_schemaRegistry.getDataSchemaNode(JUKEBOX_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap,
                m_nodesOfTypeWithinSchemaMount);
        jukeboxCTN.setMountPoint(true);

        m_deleteContainerCommand = new DeleteContainerCommand().addDeleteInfo(libraryHelper, jukeboxModelNode, libraryModelNode, EditChangeSource.system);
        m_deleteContainerCommand.appendChange(jukeboxCTN, false);

        Map<ModelNodeId, ChangeTreeNode> jukeboxCTNChildren = jukeboxCTN.getChildren();
        assertEquals(1, jukeboxCTNChildren.size());
        ChangeTreeNode libraryCTN = jukeboxCTN.getNodesOfType(LIBRARY_SCHEMA_PATH).iterator().next();
        assertEquals("library[/jukebox/library] -> delete\n" +
                " artist[/jukebox/library/artist[name='Lenny']] -> delete\n" +
                "  name -> delete { previousVal = 'lenny', currentVal = 'null' }", libraryCTN.print().trim());
        assertTrue(jukeboxCTNChildren.get(LIBRARY_MNID).isMountPoint());
        assertTrue(jukeboxCTN.getNodesOfType(ARTIST_SCHEMA_PATH).iterator().next().isMountPoint());
    }
}
