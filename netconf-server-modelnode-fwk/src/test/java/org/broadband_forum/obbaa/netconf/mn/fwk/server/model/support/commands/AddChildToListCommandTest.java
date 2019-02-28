package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LOCATION;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SINGER_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcPayloadConstraintParser;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

public class AddChildToListCommandTest {


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
        m_schemaRegistry = new SchemaRegistryImpl(yangs, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_netconfServer = new NetConfServerImpl(m_schemaRegistry, mock(RpcPayloadConstraintParser.class));
        m_yangFilePath = TestUtil.class.getResource(EXAMPLE_JUKEBOX_YANGFILE).getPath();
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelNodeDsmSpy = spy(m_modelNodeDsm);
        m_yangModel = YangUtils.createInMemoryModelNode(m_yangFilePath, new LocalSubSystem(), m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsmSpy);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsmSpy, m_subSystemRegistry);
        m_rootModelNodeAggregator.addModelServiceRoot(COMPONENT_ID, m_yangModel);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_netconfServer.setRunningDataStore(dataStore);
    }

    @Test
    public void testAddChildToListCommand() throws ModelNodeCreateException, SchemaPathBuilderException {
        QName qName = QName.create(NAMESPACE,"2014-07-03",SONG_LOCAL_NAME);
        EditContainmentNode editNode = new EditContainmentNode(qName, EditConfigOperations.CREATE);
        editNode.addMatchNode(QName.create(NAMESPACE,"2014-07-03",NAME), new GenericConfigAttribute(NAME, NAMESPACE, "Circus"));
        EditChangeNode locationCN = new EditChangeNode(QName.create(NAMESPACE,"2014-07-03",LOCATION), new GenericConfigAttribute(LOCATION, NAMESPACE, "desktop/somelocation"));
        EditChangeNode singerCN = new EditChangeNode(QName.create(NAMESPACE, "2014-07-03",SINGER_LOCAL_NAME), new GenericConfigAttribute(SINGER_LOCAL_NAME, NAMESPACE, "NewSinger"));
        EditChangeNode singer2CN = new EditChangeNode(QName.create(NAMESPACE, "2014-07-03",SINGER_LOCAL_NAME), new GenericConfigAttribute(SINGER_LOCAL_NAME, NAMESPACE, "NewSinger2"));
        editNode.addChangeNode(locationCN);
        editNode.addChangeNode(singerCN);
        editNode.addChangeNode(singer2CN);

        ModelNodeId modelNodeId = new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny", NAMESPACE);

        HelperDrivenModelNode albumModelNode  = new ModelNodeWithAttributes(ALBUM_SCHEMA_PATH, modelNodeId, m_modelNodeHelperRegistry,
                null, m_schemaRegistry, null);
        albumModelNode.setModelNodeId(new ModelNodeId("/container=jukebox/container=library/container=artist/name=Lenny/" +
                "container=album/name=Circus", NAMESPACE));
        ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(albumModelNode.getModelNodeSchemaPath(),
                editNode.getQName());
        AddChildToListCommand command = new AddChildToListCommand(new EditContext(editNode, null, null, null),
                m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor()).addAddInfo(childListHelper, albumModelNode);

        ModelNodeWithAttributes cmd = (ModelNodeWithAttributes) command.createChild();
        Assert.assertEquals(2, cmd.getAttributes().size());
        Assert.assertEquals(new GenericConfigAttribute(NAME, NAMESPACE, "Circus"), cmd.getAttribute(QName.create(NAMESPACE, "2014-07-03", NAME)));
        Assert.assertEquals(new GenericConfigAttribute(LOCATION, NAMESPACE, "desktop/somelocation"), cmd.getAttribute(QName.create(NAMESPACE, "2014-07-03", LOCATION)));
        Assert.assertNotEquals(new GenericConfigAttribute(SINGER_LOCAL_NAME, NAMESPACE, "NewSinger2"), cmd.getAttribute(QName.create(NAMESPACE, "2014-07-03", SINGER_LOCAL_NAME)));
    }
}
