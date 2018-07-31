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

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;

import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

public class ReplaceChildInListCommandTest {
    private static final String COMPONENT_ID = "Jukebox";
    private static final String EXAMPLE_JUKEBOX_YANGFILE = "/yangs/example-jukebox.yang";
    private static final String NAMESPACE = "http://example.com/ns/example-jukebox";

    private NetConfServerImpl m_netconfServer;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private SchemaRegistry m_schemaRegistry;
    private InMemoryDSM m_modelNodeDsm;
    private InMemoryDSM m_modelNodeDsmSpy;
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private String m_yangFilePath;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    private ModelNode m_yangModel;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxDeps();
        yangs.add(TestUtil.getByteSource(EXAMPLE_JUKEBOX_YANGFILE));
        m_schemaRegistry = new SchemaRegistryImpl(yangs, new NoLockService());
        m_netconfServer = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_yangFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_YANGFILE).getPath();
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeDsmSpy = spy(m_modelNodeDsm);
        m_yangModel = YangUtils.createInMemoryModelNode(m_yangFilePath, new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsmSpy);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
                m_modelNodeDsmSpy, m_subSystemRegistry);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, m_yangModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_netconfServer.setRunningDataStore(dataStore);
    }

    @Test
    public void testReplaceChildInListCommand() throws ModelNodeCreateException, SchemaPathBuilderException {
        QName qName = QName.create(NAMESPACE, "2014-07-03", "song");
        EditContainmentNode editNode = new EditContainmentNode(qName, EditConfigOperations.REPLACE);
        editNode.addMatchNode(QName.create(NAMESPACE, "2014-07-03", "name"), new GenericConfigAttribute("Circus"));
        editNode.addChangeNode(QName.create(NAMESPACE, "2014-07-03", "location"), new GenericConfigAttribute
                ("desktop/somelocation"));
        editNode.addChangeNode(QName.create(NAMESPACE, "2014-07-03", "singer"), new GenericConfigAttribute
                ("NewSinger"));
        editNode.addChangeNode(QName.create(NAMESPACE, "2014-07-03", "singer"), new GenericConfigAttribute
                ("NewSinger2"));

        ModelNodeId modelNodeId = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny",
                NAMESPACE);

        HelperDrivenModelNode albumModelNode = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, modelNodeId,
                m_modelNodeHelperRegistry,
                null, m_schemaRegistry, null);
        albumModelNode.setModelNodeId(new ModelNodeId
                ("/container=jukebox/container=library/container=artist/name=Lenny/" +
                "container=album/name=Circus", NAMESPACE));
        ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(albumModelNode
                        .getModelNodeSchemaPath(),
                editNode.getQName());
        ReplaceChildInListCommand command = new ReplaceChildInListCommand(new EditContext(editNode, null, null, null),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor()).addReplaceInfo(childListHelper,
                albumModelNode, null);

        ModelNodeWithAttributes cmd = (ModelNodeWithAttributes) command.createChild();
        Assert.assertEquals(2, cmd.getAttributes().size());
        Assert.assertEquals(new GenericConfigAttribute("Circus"), cmd.getAttribute(QName.create(NAMESPACE,
                "2014-07-03", "name")));
        Assert.assertEquals(new GenericConfigAttribute("desktop/somelocation"), cmd.getAttribute(QName.create
                (NAMESPACE, "2014-07-03", "location")));
        Assert.assertNotEquals(new GenericConfigAttribute("NewSinger2"), cmd.getAttribute(QName.create(NAMESPACE,
                "2014-07-03", "singer")));
    }
}
