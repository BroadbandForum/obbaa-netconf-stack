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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.GENRE_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_NAME_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.YEAR_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.IdentityRefConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class CreateListCommandTest extends AbstractCommandTest {

    private static final ModelNodeId SINGER_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/" +
            "name=Lenny/container=album/name=Circus/container=song/name=fly away/container=singer", JB_NS);
    private static final ModelNodeId LOCATION_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/" +
            "name=Lenny/container=album/name=Circus/container=song/name=fly away/container=location", JB_NS);
    private static final ModelNodeId SONG_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/" +
            "name=Lenny/container=album/name=Circus/container=song/name=fly away", JB_NS);

    private ChildListHelper m_childListHelper;
    private CreateListCommand m_createListCommand;

    @Test
    public void testCreateList() throws ModelNodeCreateException, SchemaPathBuilderException {
        initializeSongData();

        ModelNodeWithAttributes songModelNode = (ModelNodeWithAttributes) m_createListCommand.createChild();

        assertEquals(2, songModelNode.getAttributes().size());
        assertEquals(new GenericConfigAttribute(NAME, JB_NS, "fly away"), songModelNode.getAttribute(NAME_QNAME));
        assertEquals(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"), songModelNode.getAttribute(LOCATION_QNAME));
        assertNull(songModelNode.getAttribute(SINGER_QNAME));
        assertEquals(0, songModelNode.getLeafLists().size());
    }

    @Test
    public void testCreateOrderedByList() throws ModelNodeCreateException, SchemaPathBuilderException {
        initializeOrderedByAlbumData(InsertOperation.FIRST_OP);

        ModelNodeWithAttributes albumModelNode = (ModelNodeWithAttributes) m_createListCommand.createChild();

        assertEquals(3, albumModelNode.getAttributes().size());
        assertEquals(new GenericConfigAttribute(NAME, JB_NS, "Greatest Hits"), albumModelNode.getAttribute(NAME_QNAME));
        assertEquals(new IdentityRefConfigAttribute(JB_NS, "jbox", DocumentUtils.createDocument()
                .createElementNS(JB_NS, "blues")), albumModelNode.getAttribute(GENRE_QNAME));
        assertEquals(new GenericConfigAttribute(YEAR, JB_NS, "2000"), albumModelNode.getAttribute(YEAR_QNAME));
    }

    @Test
    public void testAppendChangesAddsCreateInfo() {
        initializeSongData();
        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN,
                m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        albumCTN.setMountPoint(true);

        m_createListCommand.appendChange(albumCTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumTNChildren = albumCTN.getChildren();
        assertEquals(1, albumTNChildren.size());
        assertEquals("song[/jukebox/library/artist[name='Lenny']/album[name='Circus']/song[name='fly away']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'fly away' }\n" +
                " location -> create { previousVal = 'null', currentVal = 'desktop/somelocation' }\n" +
                " singer -> create { previousVal = 'null', currentVal = 'NewSinger' }\n" +
                " singer -> create { previousVal = 'null', currentVal = 'NewSinger2' }", albumCTN.getNodesOfType(SONG_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode songCTN = albumTNChildren.get(SONG_MNID);
        assertFalse(songCTN.isImplied());
        assertEquals(EditConfigOperations.CREATE, songCTN.getEditOperation());
        assertEquals(EditChangeSource.user, songCTN.getChangeSource());
        assertEquals(EditChangeSource.system, songCTN.getChildren().get(SINGER_MNID).getChangeSource());
        assertEquals(EditChangeSource.user, songCTN.getChildren().get(LOCATION_MNID).getChangeSource());
        assertTrue(albumCTN.getNodesOfType(SONG_SCHEMA_PATH).iterator().next().isMountPoint());
        assertTrue(albumCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
        assertTrue(albumCTN.getNodesOfType(LOCATION_SCHEMA_PATH).iterator().next().isMountPoint());
        assertTrue(albumCTN.getNodesOfType(SONG_NAME_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAddsCreateInfoWithInsertOperationSetToFirst() {
        initializeOrderedByAlbumData(InsertOperation.FIRST_OP);
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_createListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'Greatest Hits' }\n" +
                " genre -> create { previousVal = 'null', currentVal = '' }\n" +
                " year -> create { previousVal = 'null', currentVal = '2000' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertNull(albumTN.getInsertOperation().getValue());
        assertEquals(InsertOperation.FIRST, albumTN.getInsertOperation().getName());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAddsCreateInfoWithInsertOperationSetToLast() {
        initializeOrderedByAlbumData(InsertOperation.LAST_OP);
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_createListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'Greatest Hits' }\n" +
                " genre -> create { previousVal = 'null', currentVal = '' }\n" +
                " year -> create { previousVal = 'null', currentVal = '2000' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertEquals(InsertOperation.LAST, albumTN.getInsertOperation().getName());
        assertNull(albumTN.getInsertOperation().getValue());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAddsCreateInfoWithInsertOperationSetToBefore() {
        initializeOrderedByAlbumData(InsertOperation.get("before", "Circus"));
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_createListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'Greatest Hits' }\n" +
                " genre -> create { previousVal = 'null', currentVal = '' }\n" +
                " year -> create { previousVal = 'null', currentVal = '2000' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertEquals(InsertOperation.BEFORE, albumTN.getInsertOperation().getName());
        assertEquals("Circus", albumTN.getInsertOperation().getValue());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAddsCreateInfoWithInsertOperationSetToAfter() {
        initializeOrderedByAlbumData(InsertOperation.get("after", "Circus"));
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_createListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'Greatest Hits' }\n" +
                " genre -> create { previousVal = 'null', currentVal = '' }\n" +
                " year -> create { previousVal = 'null', currentVal = '2000' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertEquals(InsertOperation.AFTER, albumTN.getInsertOperation().getName());
        assertEquals("Circus", albumTN.getInsertOperation().getValue());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAddsCreateInfoWithoutUpdatingInsertOperationIfNotSpecified() {
        initializeOrderedByAlbumData(null);
        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(false);

        m_createListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistTNChildren = artistCTN.getChildren();
        assertEquals(1, artistTNChildren.size());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Greatest Hits']] -> create\n" +
                " name -> create { previousVal = 'null', currentVal = 'Greatest Hits' }\n" +
                " genre -> create { previousVal = 'null', currentVal = '' }\n" +
                " year -> create { previousVal = 'null', currentVal = '2000' }", artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator()
                .next().print().trim());
        ChangeTreeNode albumTN = artistTNChildren.get(GREATEST_HITS_ID);
        assertNull(albumTN.getInsertOperation());
        assertFalse(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    private void initializeSongData() {
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.CREATE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "fly away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"));
        EditChangeNode singerCN = new EditChangeNode(SINGER_QNAME, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger"));
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2"));
        locationCN.setChangeSource(EditChangeSource.user);
        singerCN.setChangeSource(EditChangeSource.system);
        singer2CN.setChangeSource(EditChangeSource.system);
        editNode.addChangeNode(locationCN);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);
        editNode.setChangeSource(EditChangeSource.user);

        HelperDrivenModelNode albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, LENNY_MNID, m_modelNodeHelperRegistry,
                null, m_schemaRegistry, null);
        albumModelNode.setModelNodeId(CIRCUS_MNID);

        m_childListHelper = m_modelNodeHelperRegistry.getChildListHelper(albumModelNode.getModelNodeSchemaPath(), editNode.getQName());

        m_createListCommand = new CreateListCommand(new EditContext(editNode, null, null, null),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), null).addAddInfo(m_childListHelper, albumModelNode);
    }

    private void initializeOrderedByAlbumData(InsertOperation insertOperation) {
        EditContainmentNode editNode = new TestEditContainmentNode(ALBUM_QNAME, EditConfigOperations.CREATE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Greatest Hits"));
        EditChangeNode genreCN = new EditChangeNode(GENRE_QNAME, new IdentityRefConfigAttribute(JB_NS, "jbox",
                DocumentUtils.createDocument().createElementNS(JB_NS, "blues")));
        EditChangeNode yearCN = new EditChangeNode(YEAR_QNAME, new GenericConfigAttribute(YEAR, JB_NS, "2000"));
        editNode.addChangeNode(genreCN);
        editNode.addChangeNode(yearCN);
        editNode.setInsertOperation(insertOperation);

        HelperDrivenModelNode artistModelNode = new ModelNodeWithAttributes(ARTIST_SCHEMA_PATH, LIBRARY_MNID, m_modelNodeHelperRegistry,
                null, m_schemaRegistry, null);
        artistModelNode.setModelNodeId(LENNY_MNID);

        m_childListHelper = m_modelNodeHelperRegistry.getChildListHelper(artistModelNode.getModelNodeSchemaPath(),
                editNode.getQName());

        m_createListCommand = new CreateListCommand(new EditContext(editNode, null, null, null),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), null).addAddInfo(m_childListHelper, artistModelNode);
    }
}
