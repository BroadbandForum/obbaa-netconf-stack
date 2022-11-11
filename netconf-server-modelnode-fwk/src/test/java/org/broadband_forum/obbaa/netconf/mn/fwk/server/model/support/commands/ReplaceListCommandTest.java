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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_SCHEMA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;

@RunWith(RequestScopeJunitRunner.class)
public class ReplaceListCommandTest extends AbstractCommandTest {

    private static final ModelNodeId FLY_AWAY_SONG_MNID = new ModelNodeId("/container=jukebox/container=library/" +
            "container=artist/name=Lenny/container=album/name=Greatest hits/container=song/name=Fly Away", JB_NS);
    private static final ModelNodeId CIRCUS_SONG_MNID = new ModelNodeId("/container=jukebox/container=library/" +
            "container=artist/name=Lenny/container=album/name=Circus/container=song/name=Circus", JB_NS);

    private ChildListHelper m_childListHelper;
    private ReplaceListCommand m_replaceListCommand;

    @Test
    public void testReplaceChildInListCommand() throws SchemaPathBuilderException, CommandExecutionException {
        EditContainmentNode editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.REPLACE, m_schemaRegistry);
        editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Fly Away"));
        EditChangeNode locationCN = new EditChangeNode(LOCATION_QNAME, new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"));
        ConfigLeafAttribute firstSinger = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger");
        ConfigLeafAttribute secondSinger = new GenericConfigAttribute(SINGER_LOCAL_NAME, JB_NS, "NewSinger2");
        EditChangeNode singerCN = new EditChangeNode(SINGER_QNAME, firstSinger);
        EditChangeNode singer2CN = new EditChangeNode(SINGER_QNAME, secondSinger);
        editNode.addChangeNode(locationCN);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);

        ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(m_albumNode.getModelNodeSchemaPath(),
                editNode.getQName());

        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_replaceListCommand = new ReplaceListCommand(new EditContext(editNode, m_notificationContext,
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), albumCTN).addReplaceInfo(childListHelper,
                m_albumNode, m_songNode);

        ModelNodeWithAttributes replacedSongNode = (ModelNodeWithAttributes) m_replaceListCommand.replaceChild();

        Set<ConfigLeafAttribute> leafListAttributes = new LinkedHashSet<>();
        leafListAttributes.add(firstSinger);
        leafListAttributes.add(secondSinger);

        assertEquals(2, replacedSongNode.getAttributes().size());
        assertEquals(new GenericConfigAttribute(NAME, JB_NS, "Fly Away"), replacedSongNode.getAttribute(QName.create(JB_NS, JB_REVISION, NAME)));
        assertEquals(new GenericConfigAttribute(LOCATION, JB_NS, "desktop/somelocation"),
                replacedSongNode.getAttribute(QName.create(JB_NS, JB_REVISION, LOCATION)));
        assertNull(replacedSongNode.getAttribute(QName.create(JB_NS, JB_REVISION, SINGER_LOCAL_NAME)));
        assertEquals(leafListAttributes, replacedSongNode.getLeafList(SINGER_QNAME));
    }

    @Test
    public void testAppendChangesAppendsChangeInfoForExistingList() {
        initializeSongPrerequisites();
        m_editNode.setInsertOperation(InsertOperation.FIRST_OP);
        when(m_childListHelper.isOrderUpdateNeededForChild(anyCollection(), any(ModelNode.class), anyInt())).thenReturn(true);

        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_replaceListCommand = new ReplaceListCommand(new EditContext(m_editNode, m_notificationContext,
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), albumCTN)
                .addReplaceInfo(m_childListHelper, m_albumNode, m_songNode);

        m_replaceListCommand.appendChange(albumCTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumTNChildren = albumCTN.getChildren();
        assertEquals(1, albumTNChildren.size());
        ChangeTreeNode songTN = albumTNChildren.get(FLY_AWAY_SONG_MNID);
        assertNull(songTN.getInsertOperation());
        assertEquals("song[/jukebox/library/artist[name='Lenny']/album[name='Greatest hits']/song[name='Fly Away']] -> modify\n" +
                " name -> none { previousVal = 'Fly Away', currentVal = 'Fly Away' }", songTN.print().trim());
        assertFalse(songTN.isImplied());
        assertEquals(EditConfigOperations.REPLACE, songTN.getEditOperation());
        assertEquals(EditChangeSource.system, songTN.getChangeSource());
        assertFalse(albumCTN.getNodesOfType(SONG_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesAppendsInsertOperationChangeInfoForOrderedByList() {
        initializeOrderedByAlbumData();
        m_editNode.setInsertOperation(InsertOperation.FIRST_OP);
        ModelNode albumModelNode = mock(ModelNode.class);
        when(m_childListHelper.isOrderUpdateNeededForChild(anyCollection(), any(ModelNode.class), anyInt())).thenReturn(true);

        WritableChangeTreeNode artistCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, LENNY_MNID, m_artistDSN,
                m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        artistCTN.setMountPoint(true);

        m_replaceListCommand = new ReplaceListCommand(new EditContext(m_editNode, m_notificationContext,
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), artistCTN)
                .addReplaceInfo(m_childListHelper, m_artistNode, albumModelNode);

        m_replaceListCommand.appendChange(artistCTN, false);

        Map<ModelNodeId, ChangeTreeNode> artistChildren = artistCTN.getChildren();
        assertEquals(1, artistChildren.size());
        ChangeTreeNode albumCTN = artistChildren.get(CIRCUS_MNID);
        assertNull(albumCTN.getInsertOperation().getValue());
        assertEquals("first", albumCTN.getInsertOperation().getName());
        assertEquals("album[/jukebox/library/artist[name='Lenny']/album[name='Circus']] -> modify\n" +
                " name -> none { previousVal = 'Circus', currentVal = 'Circus' }", albumCTN.print().trim());
        assertTrue(artistCTN.getNodesOfType(ALBUM_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesDoesNotAppendAnyChangesForNoChange() {
        initializeSongPrerequisites();
        when(m_childListHelper.isOrderUpdateNeededForChild(anyCollection(), any(ModelNode.class), anyInt())).thenReturn(false);

        WritableChangeTreeNode albumTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        m_replaceListCommand = new ReplaceListCommand(new EditContext(m_editNode, m_notificationContext,
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), albumTN)
                .addReplaceInfo(m_childListHelper, m_albumNode, m_songNode);

        m_replaceListCommand.appendChange(albumTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumTNChildren = albumTN.getChildren();
        assertEquals(0, albumTNChildren.size());
    }

    private void initializeSongPrerequisites() {
        m_editNode = new TestEditContainmentNode(SONG_QNAME, EditConfigOperations.REPLACE, m_schemaRegistry);
        m_editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Fly Away"));
        m_editNode.setChangeSource(EditChangeSource.system);

        m_childListHelper = spy(m_modelNodeHelperRegistry.getChildListHelper(m_albumNode.getModelNodeSchemaPath(), m_editNode.getQName()));

    }

    private void initializeOrderedByAlbumData() {
        m_editNode = new TestEditContainmentNode(ALBUM_QNAME, EditConfigOperations.REPLACE, m_schemaRegistry);
        m_editNode.addMatchNode(NAME_QNAME, new GenericConfigAttribute(NAME, JB_NS, "Circus"));

        m_childListHelper = spy(m_modelNodeHelperRegistry.getChildListHelper(m_artistNode.getModelNodeSchemaPath(), m_editNode.getQName()));
    }
}
