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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADMIN_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADMIN_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.GENRE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.GENRE_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class DeleteListCommandTest extends AbstractCommandTest {
    private static final ModelNodeId YEAR_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=dummy-album/container=year", JB_NS);
    private static final ModelNodeId ADMIN_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=circus/container=admin", JB_NS);
    private static final ModelNodeId DUMMY_ALBUM_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=dummy-album", JB_NS);

    private DeleteListCommand m_deleteListCommand;

    @Mock
    private ChildListHelper m_childListHelper;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException, SetAttributeException {
        m_deleteListCommand = new DeleteListCommand().addRemoveInfo(m_childListHelper, m_libraryNode, m_artistNode, EditChangeSource.system);
        when(m_childListHelper.getSchemaNode()).thenReturn((ListSchemaNode) m_artistDSN);
    }

    @Test
    public void testDeleteList() throws SchemaPathBuilderException, CommandExecutionException, ModelNodeDeleteException {
        m_deleteListCommand.execute();
        verify(m_childListHelper).removeChild(m_libraryNode, m_artistNode);
    }

    @Test
    public void testAppendChangesAddsDeleteInfo() {
        WritableChangeTreeNode libraryTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LIBRARY_MNID, m_libraryDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_deleteListCommand.appendChange(libraryTN, false);

        Map<ModelNodeId, ChangeTreeNode> libraryTNChildren = libraryTN.getChildren();
        assertEquals(1, libraryTNChildren.size());
        assertEquals("artist[/jukebox/library/artist[name='Lenny']] -> delete\n" +
                " name -> delete { previousVal = 'Lenny', currentVal = 'null' }", libraryTN.getNodesOfType(ARTIST_SCHEMA_PATH)
                .iterator().next().print().trim());
        assertFalse(libraryTNChildren.get(LENNY_MNID).isImplied());
        assertEquals(EditChangeSource.system, libraryTNChildren.get(LENNY_MNID).getChangeSource());
    }

    @Test
    public void testAppendChangesAddsPreviousLeafValues() {
        ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        ModelNode albumModelNode = mock(ModelNode.class);
        ChildListHelper albumHelper = mock(ChildListHelper.class);

        m_deleteListCommand = new DeleteListCommand().addRemoveInfo(albumHelper, m_artistNode, albumModelNode, EditChangeSource.system);
        when(albumModelNode.getModelNodeId()).thenReturn(DUMMY_ALBUM_MNID);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeSchemaPath()).thenReturn(ALBUM_SCHEMA_PATH);
        when(albumHelper.getSchemaNode()).thenReturn((ListSchemaNode) m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH));
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN,
                m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        Map<QName, ConfigLeafAttribute> attributes = new LinkedHashMap<>();
        attributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "dummy-album"));
        attributes.put(GENRE_QNAME, new GenericConfigAttribute(GENRE, JB_NS, "jbox:jazz"));
        attributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1999"));
        when(albumModelNode.getAttributes()).thenReturn(attributes);
        when(albumModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);

        ConfigAttributeHelper nameHelper = mock(ConfigAttributeHelper.class);
        LeafSchemaNode nameSchemaNode = mock(LeafSchemaNode.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, NAME_QNAME)).thenReturn(nameHelper);
        when(nameHelper.getLeafSchemaNode()).thenReturn(nameSchemaNode);
        when(nameSchemaNode.getQName()).thenReturn(NAME_QNAME);

        LeafSchemaNode genreLeafSchemaNode = mock(LeafSchemaNode.class);
        ConfigAttributeHelper genreHelper = mock(ConfigAttributeHelper.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, GENRE_QNAME)).thenReturn(genreHelper);
        when(genreHelper.getLeafSchemaNode()).thenReturn(genreLeafSchemaNode);
        when(genreLeafSchemaNode.getQName()).thenReturn(GENRE_QNAME);

        LeafSchemaNode yearSchemaNode = mock(LeafSchemaNode.class);
        ConfigAttributeHelper yearHelper = mock(ConfigAttributeHelper.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, YEAR_QNAME)).thenReturn(yearHelper);
        when(yearHelper.getLeafSchemaNode()).thenReturn(yearSchemaNode);
        when(yearSchemaNode.getQName()).thenReturn(YEAR_QNAME);

        m_deleteListCommand.appendChange(artistCTN, false);
        Map<ModelNodeId, ChangeTreeNode> artistCTNChildren = artistCTN.getChildren();
        assertEquals(1, artistCTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='dummy-album']] -> delete\n" +
                " name -> delete { previousVal = 'dummy-album', currentVal = 'null' }\n" +
                " genre -> delete { previousVal = 'jbox:jazz', currentVal = 'null' }\n" +
                " year -> delete { previousVal = '1999', currentVal = 'null' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().print().trim());
        ChangeTreeNode albumCTN = artistCTNChildren.get(DUMMY_ALBUM_MNID);
        assertFalse(albumCTN.isImplied());
        assertEquals(EditChangeSource.system, albumCTN.getChangeSource());
        assertEquals(EditChangeSource.system, albumCTN.getChildren().get(YEAR_MNID).getChangeSource());
    }

    @Test
    public void testAppendChangesDoesNotAddChildNodesWhenCurrentNodeIsNotUnderSchemaMount() {
        ModelNode artistModelNode = mock(ModelNode.class);
        ModelNode albumModelNode = mock(ModelNode.class);

        ConfigAttributeHelper nameHelper = mock(ConfigAttributeHelper.class);
        SchemaPath nameSP = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(NAME).build();
        when(nameHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(nameSP));

        ConfigAttributeHelper yearHelper = mock(ConfigAttributeHelper.class);
        SchemaPath yearSP = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(YEAR).build();
        when(yearHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(yearSP));

        ChildListHelper albumHelper = mock(ChildListHelper.class);
        when(albumHelper.getSchemaNode()).thenReturn((ListSchemaNode) m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH));

        ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, NAME_QNAME)).thenReturn(nameHelper);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, YEAR_QNAME)).thenReturn(yearHelper);

        Map<QName, ConfigLeafAttribute> albumAttributes = new LinkedHashMap<>();
        albumAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "lenny"));
        albumAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1996"));
        when(artistModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeId()).thenReturn(CIRCUS_MNID);
        when(albumModelNode.getAttributes()).thenReturn(albumAttributes);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);
        when(albumModelNode.getModelNodeSchemaPath()).thenReturn(ALBUM_SCHEMA_PATH);

        m_deleteListCommand = new DeleteListCommand().addRemoveInfo(albumHelper, artistModelNode, albumModelNode, EditChangeSource.system);

        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID,
                m_schemaRegistry.getDataSchemaNode(ARTIST_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_deleteListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistCTNChildren = artistCTN.getChildren();
        assertEquals(1, artistCTNChildren.size());
        ChangeTreeNode albumCTN = artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next();
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> delete\n" +
                " name -> delete { previousVal = 'lenny', currentVal = 'null' }\n" +
                " year -> delete { previousVal = '1996', currentVal = 'null' }", albumCTN.print().trim());
        assertFalse(artistCTNChildren.get(CIRCUS_MNID).isMountPoint());
    }

    @Test
    public void testAppendChangesAddsChildNodesWhenCurrentNodeIsUnderSchemaMount() {
        ModelNode artistModelNode = mock(ModelNode.class);
        XmlModelNodeImpl albumModelNode = mock(XmlModelNodeImpl.class);
        XmlModelNodeImpl adminModelNode = mock(XmlModelNodeImpl.class);

        ConfigAttributeHelper albumNameHelper = mock(ConfigAttributeHelper.class);
        SchemaPath albumNameSP = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(NAME).build();
        when(albumNameHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(albumNameSP));

        ConfigAttributeHelper yearHelper = mock(ConfigAttributeHelper.class);
        SchemaPath yearSP = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(YEAR).build();
        when(yearHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(yearSP));

        ConfigAttributeHelper labelHelper = mock(ConfigAttributeHelper.class);
        SchemaPath labelSP = new SchemaPathBuilder().withParent(ADMIN_SCHEMA_PATH).appendLocalName(LABEL).build();
        when(labelHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(labelSP));

        ConfigAttributeHelper catalogueHelper = mock(ConfigAttributeHelper.class);
        SchemaPath catalogueNumberSP = new SchemaPathBuilder().withParent(ADMIN_SCHEMA_PATH).appendLocalName(CATALOGUE_NUMBER).build();
        when(catalogueHelper.getLeafSchemaNode()).thenReturn((LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(catalogueNumberSP));

        ChildListHelper albumHelper = mock(ChildListHelper.class);
        when(albumHelper.getSchemaNode()).thenReturn((ListSchemaNode) m_schemaRegistry.getDataSchemaNode(ALBUM_SCHEMA_PATH));

        ModelNodeHelperRegistry modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, NAME_QNAME)).thenReturn(albumNameHelper);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ALBUM_SCHEMA_PATH, YEAR_QNAME)).thenReturn(yearHelper);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ADMIN_SCHEMA_PATH, LABEL_QNAME)).thenReturn(labelHelper);
        when(modelNodeHelperRegistry.getConfigAttributeHelper(ADMIN_SCHEMA_PATH, CATALOGUE_NUMBER_QNAME)).thenReturn(catalogueHelper);

        Map<QName, ConfigLeafAttribute> albumAttributes = new LinkedHashMap<>();
        albumAttributes.put(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "lenny"));
        albumAttributes.put(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "1996"));

        Map<QName, ConfigLeafAttribute> adminAttributes = new LinkedHashMap<>();
        adminAttributes.put(LABEL_QNAME, new GenericConfigAttribute(LABEL, JB_NS, "sony"));
        adminAttributes.put(CATALOGUE_NUMBER_QNAME, new GenericConfigAttribute(CATALOGUE_NUMBER, JB_NS, "1"));

        when(adminModelNode.getKey()).thenReturn(ADMIN_MNID);
        IndexedList admin = new IndexedList<ModelNodeId, XmlModelNodeImpl>();
        admin.add(adminModelNode);
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> adminMap = new HashMap<>();
        adminMap.put(ADMIN_QNAME, admin);

        WritableChangeTreeNode adminCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, ADMIN_MNID,
                m_schemaRegistry.getDataSchemaNode(ADMIN_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        adminCTN.setChangeType(ChangeTreeNode.ChangeType.delete);

        when(artistModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(albumModelNode.getModelNodeId()).thenReturn(CIRCUS_MNID);
        when(albumModelNode.getAttributes()).thenReturn(albumAttributes);
        when(albumModelNode.getChildren()).thenReturn(adminMap);
        when(albumModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);
        when(albumModelNode.getModelNodeSchemaPath()).thenReturn(ALBUM_SCHEMA_PATH);

        when(adminModelNode.buildCtnForDelete(any(WritableChangeTreeNode.class))).thenReturn(adminCTN);
        when(adminModelNode.getAttributes()).thenReturn(adminAttributes);
        when(adminModelNode.getModelNodeId()).thenReturn(ADMIN_MNID);
        when(adminModelNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(adminModelNode.getMountModelNodeHelperRegistry()).thenReturn(modelNodeHelperRegistry);
        when(adminModelNode.getModelNodeSchemaPath()).thenReturn(ADMIN_SCHEMA_PATH);

        m_deleteListCommand = new DeleteListCommand().addRemoveInfo(albumHelper, artistModelNode, albumModelNode, EditChangeSource.system);

        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID,
                m_schemaRegistry.getDataSchemaNode(ARTIST_SCHEMA_PATH), m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(true);

        m_deleteListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistCTNChildren = artistCTN.getChildren();
        assertEquals(1, artistCTNChildren.size());
        ChangeTreeNode albumCTN = artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next();
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> delete\n" +
                " admin[/jukebox/library/artist[name='Lenny']/album[name='circus']/admin] -> delete\n" +
                "  label -> delete { previousVal = 'sony', currentVal = 'null' }\n" +
                "  catalogue-number -> delete { previousVal = '1', currentVal = 'null' }\n" +
                " name -> delete { previousVal = 'lenny', currentVal = 'null' }\n" +
                " year -> delete { previousVal = '1996', currentVal = 'null' }", albumCTN.print().trim());
        assertTrue(artistCTNChildren.get(CIRCUS_MNID).isMountPoint());
        assertTrue(artistCTN.getNodesOfType(ADMIN_SCHEMA_PATH).iterator().next().isMountPoint());
    }
}
