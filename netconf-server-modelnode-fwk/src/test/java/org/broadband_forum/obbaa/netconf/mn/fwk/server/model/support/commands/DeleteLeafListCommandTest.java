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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_REVISION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.AttributeIndex;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

@RunWith(RequestScopeJunitRunner.class)
public class DeleteLeafListCommandTest extends AbstractCommandTest {

    private static final ModelNodeId SONG_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist" +
            "/name='Lenny'/container=album/name='circus'/container=song/name='fly away'", JB_NS);
    private static final ModelNodeId SINGER_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist" +
            "/name='Lenny'/container=album/name='circus'/container=song/name='fly away'/container=singer", JB_NS);

    private DeleteLeafListCommand m_deleteLeafListCommand;

    @Mock
    private ChildLeafListHelper m_childLeafListHelper;
    @Mock
    private HelperDrivenModelNode m_songNode;
    @Mock
    private ConfigLeafAttribute m_firstSinger;
    @Mock
    private ConfigLeafAttribute m_secondSinger;

    @Before
    public void setUp() throws Exception {
        when(m_songNode.getSchemaRegistry()).thenReturn(m_schemaRegistry);
        when(m_songNode.getQName()).thenReturn(QName.create(JB_NS, JB_REVISION, SONG_LOCAL_NAME));
        when(m_songNode.getModelNodeId()).thenReturn(SONG_MNID);
        when(m_childLeafListHelper.getLeafListSchemaNode()).thenReturn((LeafListSchemaNode) m_schemaRegistry.getDataSchemaNode(SINGER_SCHEMA_PATH));
        Set<ConfigLeafAttribute> m_singerLists = new LinkedHashSet<>();
        m_singerLists.add(m_firstSinger);
        m_singerLists.add(m_secondSinger);
        when(m_songNode.getLeafList(SINGER_SCHEMA_PATH.getLastComponent())).thenReturn(m_singerLists);
        when(m_firstSinger.getStringValue()).thenReturn("alpha");
        when(m_secondSinger.getStringValue()).thenReturn("beta");

        m_deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo(m_childLeafListHelper, m_songNode, EditChangeSource.system);
    }

    @Test
    public void testExecute() throws CommandExecutionException, ModelNodeDeleteException {
        m_deleteLeafListCommand.execute();
        verify(m_childLeafListHelper).removeAllChild(m_songNode);
        doThrow(new ModelNodeDeleteException("Deleting leaf-list failed")).when(m_childLeafListHelper).removeAllChild(m_songNode);

        try {
            m_deleteLeafListCommand.execute();
            fail();
        } catch (Exception e) {
            assertTrue(e instanceof CommandExecutionException);
            assertEquals("org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException: Deleting leaf-list failed",
                    e.getMessage());
        }
    }

    @Test
    public void testAppendChildAppendsDeleteInfo() {
        WritableChangeTreeNode songCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, SONG_MNID, m_songDSN, m_nodesOfType, m_attrIndex,
                m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        songCTN.setMountPoint(false);

        m_deleteLeafListCommand.appendChange(songCTN, false);

        Map<ModelNodeId, ChangeTreeNode> songNodeChildren = songCTN.getChildren();
        assertEquals(1, songNodeChildren.size());
        assertEquals("singer -> delete { previousVal = 'alpha', currentVal = 'null' }\nsinger -> delete { previousVal = 'beta', currentVal = 'null' }",
                songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().print().trim());
        assertEquals("song[/jukebox/library/artist[name='Lenny']/album[name='circus']/song[name='fly away']] -> modify\n" +
                " singer -> delete { previousVal = 'alpha', currentVal = 'null' }\n singer -> delete { previousVal = 'beta', currentVal = 'null' }",
                songCTN.getNodesOfType(new AttributeIndex(SINGER_SCHEMA_PATH, m_firstSinger)).iterator().next().print().trim());
        assertFalse(songNodeChildren.get(SINGER_MNID).isImplied());
        assertEquals(EditChangeSource.system, songNodeChildren.get(SINGER_MNID).getChangeSource());
        assertFalse(songCTN.getNodesOfType(SINGER_SCHEMA_PATH).iterator().next().isMountPoint());
    }
}
