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
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CATALOGUE_NUMBER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LABEL_QNAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNodeImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.TestEditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class ReplaceContainerCommandTest extends AbstractCommandTest {

    private static final ModelNodeId ADMIN_MNID = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
            "container=album/name=Circus/container=admin", JB_NS);

    private ChildContainerHelper m_childContainerHelper;
    private HelperDrivenModelNode m_albumModelNode;

    @Test
    public void testReplaceContainerCommand() throws SchemaPathBuilderException, CommandExecutionException {
        populateEditContainmentNodeForAdmin();

        m_albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, LENNY_MNID, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, null);
        m_albumModelNode.setModelNodeId(CIRCUS_MNID);

        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(m_albumModelNode.getModelNodeSchemaPath(), m_editNode.getQName());

        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType, m_attrIndex,
                m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        ReplaceContainerCommand command = new ReplaceContainerCommand(new EditContext(m_editNode, new NotificationContext(),
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo), m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(), albumCTN)
                .addReplaceInfo(childContainerHelper, m_albumModelNode);

        ModelNodeWithAttributes replacedAdminModelNode = (ModelNodeWithAttributes) command.replaceChild();

        assertEquals(2, replacedAdminModelNode.getAttributes().size());
        assertEquals(new GenericConfigAttribute(LABEL, JB_NS, "dummy-label"), replacedAdminModelNode.getAttribute(LABEL_QNAME));
        assertEquals(new GenericConfigAttribute(CATALOGUE_NUMBER, JB_NS, "catalogue-1"), replacedAdminModelNode.getAttribute(CATALOGUE_NUMBER_QNAME));
    }

    @Test
    public void testAppendChangesAppendsChangeInfoForExistingContainer() {
        initializePrerequisites();

        CompositeEditCommand compositeCommand = new CompositeEditCommand();
        compositeCommand.appendCommand(mock(Command.class));
        compositeCommand.appendCommand(mock(Command.class));

        createMockAdminModelNode(compositeCommand);

        ReplaceContainerCommand command = new ReplaceContainerCommand(new EditContext(m_editNode, new NotificationContext(),
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo), m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(),
                null).addReplaceInfo(m_childContainerHelper, m_albumModelNode);

        WritableChangeTreeNode albumCTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        command.appendChange(albumCTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumTNChildren = albumCTN.getChildren();
        assertEquals(1, albumTNChildren.size());
        ChangeTreeNode adminTN = albumTNChildren.get(ADMIN_MNID);
        assertEquals("admin[/jukebox/library/artist[name='Lenny']/album[name='Circus']/admin] -> none", adminTN.print().trim());
        assertFalse(adminTN.isImplied());
        assertEquals(EditConfigOperations.REPLACE, adminTN.getEditOperation());
        assertEquals(EditChangeSource.user, adminTN.getChangeSource());
        assertFalse(albumCTN.getNodesOfType(ADMIN_SCHEMA_PATH).iterator().next().isMountPoint());
    }

    @Test
    public void testAppendChangesDoesNotAppendAnyChangeInfoForNoDeltaChange() {
        initializePrerequisites();

        CompositeEditCommand compositeCommand = mock(CompositeEditCommand.class);
        when(compositeCommand.getCommands()).thenReturn(Collections.emptyList());

        createMockAdminModelNode(compositeCommand);

        ReplaceContainerCommand command = new ReplaceContainerCommand(new EditContext(m_editNode, new NotificationContext(),
                EditConfigErrorOptions.ROLLBACK_ON_ERROR, m_clientInfo), m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor(),
                null).addReplaceInfo(m_childContainerHelper, m_albumModelNode);

        WritableChangeTreeNode albumTN = new ChangeTreeNodeImpl(m_schemaRegistry, null, CIRCUS_MNID, m_albumDSN, m_nodesOfType,
                m_attrIndex, m_changedNodeSPs, m_contextMap, m_nodesOfTypeWithinSchemaMount);

        command.appendChange(albumTN, false);

        Map<ModelNodeId, ChangeTreeNode> albumTNChildren = albumTN.getChildren();
        assertEquals(0, albumTNChildren.size());
    }

    private void initializePrerequisites() {
        populateEditContainmentNodeForAdmin();
        m_editNode.setChangeSource(EditChangeSource.user);

        m_albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, LENNY_MNID, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, null);
        m_albumModelNode.setModelNodeId(CIRCUS_MNID);

        m_childContainerHelper = spy(m_modelNodeHelperRegistry.getChildContainerHelper(m_albumModelNode.getModelNodeSchemaPath(), m_editNode.getQName()));
    }

    private void populateEditContainmentNodeForAdmin() {
        m_editNode = new TestEditContainmentNode(ADMIN_QNAME, EditConfigOperations.REPLACE, m_schemaRegistry);
        EditChangeNode labelCN = new EditChangeNode(LABEL_QNAME, new GenericConfigAttribute(LABEL, JB_NS, "dummy-label"));
        EditChangeNode catalogueCN = new EditChangeNode(CATALOGUE_NUMBER_QNAME, new GenericConfigAttribute(CATALOGUE_NUMBER, JB_NS, "catalogue-1"));
        m_editNode.addChangeNode(labelCN);
        m_editNode.addChangeNode(catalogueCN);
    }

    private void createMockAdminModelNode(CompositeEditCommand compositeCommand) {
        HelperDrivenModelNode adminModelNode = mock(HelperDrivenModelNode.class);
        when(m_childContainerHelper.getValue(m_albumModelNode)).thenReturn(adminModelNode);
        when(adminModelNode.getEditCommand(any(EditContext.class), any(WritableChangeTreeNode.class))).thenReturn(compositeCommand);
    }
}
