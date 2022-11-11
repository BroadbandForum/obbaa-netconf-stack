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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;

@RunWith(RequestScopeJunitRunner.class)
public class CreateContainerCommandTest extends AbstractCommandTest {

    private CreateContainerCommand m_createContainerCommand;

    @Mock
    public ChildContainerHelper m_childContainerHelper;

    @Before
    public void setUp() throws Exception {
        m_createContainerCommand = new CreateContainerCommand(m_editContext, null, null).addCreateInfo(m_childContainerHelper, m_jukeboxNode);
        when(m_childContainerHelper.getSchemaNode()).thenReturn((ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(LIBRARY_SCHEMA_PATH));
    }

    @Test
    public void testAppendChangesAddsCreateInfo() {
        WritableChangeTreeNode jukeboxCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, JUKEBOX_MNID, m_jukeboxDSN, m_nodesOfType, m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);
        jukeboxCTN.setMountPoint(true);

        m_createContainerCommand.appendChange(jukeboxCTN, false);

        Map<ModelNodeId, ChangeTreeNode> jukeboxTNChildren = jukeboxCTN.getChildren();
        assertEquals(1, jukeboxTNChildren.size());
        assertEquals("library[/jukebox/library] -> create", jukeboxCTN.getNodesOfType(LIBRARY_SCHEMA_PATH).iterator().next().print().trim());
        ChangeTreeNode changeTreeNode = jukeboxTNChildren.get(LIBRARY_MNID);
        assertFalse(changeTreeNode.isImplied());
        assertEquals(EditConfigOperations.MERGE, changeTreeNode.getEditOperation());
        assertEquals(EditChangeSource.user, changeTreeNode.getChangeSource());
        assertTrue(jukeboxCTN.getNodesOfType(LIBRARY_SCHEMA_PATH).iterator().next().isMountPoint());
    }
}
