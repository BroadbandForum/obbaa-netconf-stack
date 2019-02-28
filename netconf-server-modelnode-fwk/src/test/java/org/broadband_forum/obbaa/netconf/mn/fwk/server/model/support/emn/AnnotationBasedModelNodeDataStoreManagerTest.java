package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.Type;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.billboard.Billboard;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.billboard.BillboardConstants;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Song;

public class AnnotationBasedModelNodeDataStoreManagerTest {

    private static final String YANG_FILE = "/referenceyangs/jukebox/example-billboard@2014-07-03.yang";

    private ModelNodeDataStoreManager m_dataStoreManager;

    private PersistenceManagerUtil m_persistenceManagerUtil;

    private EntityRegistry m_entityRegistry;

    private SchemaRegistry m_schemaRegistry;

    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;

    private SubSystemRegistry m_subSystemRegistry;

    @SuppressWarnings({ "rawtypes" })
    @Before
    public void setUp() throws AnnotationAnalysisException, SchemaBuildException {
        m_persistenceManagerUtil = mock(PersistenceManagerUtil.class);
        EntityDataStoreManager entityDataStoreManager = mock(EntityDataStoreManager.class);
        when(entityDataStoreManager.getEntityManager()).thenReturn(mock(EntityManager.class));
        when(m_persistenceManagerUtil.getEntityDataStoreManager()).thenReturn(entityDataStoreManager);
        m_entityRegistry = new EntityRegistryImpl();
        m_subSystemRegistry = new SubSystemRegistryImpl();

        List<YangTextSchemaSource> yangFiles = TestUtil.getJukeBoxDeps();
        yangFiles.add(TestUtil.getByteSource(YANG_FILE));
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
        m_modelNodeDSMRegistry = ModelNodeDSMRegistryImpl.getInstance();
        
        List<Class> classes = new ArrayList<>();
        classes.add(Billboard.class);
        EntityRegistryBuilder.updateEntityRegistry("billboard", classes, m_entityRegistry, m_schemaRegistry, null, m_modelNodeDSMRegistry);

        m_dataStoreManager = new AnnotationBasedModelNodeDataStoreManager(m_persistenceManagerUtil, m_entityRegistry, m_schemaRegistry,
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_modelNodeDSMRegistry);
        m_dataStoreManager = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, m_dataStoreManager);
    }

    /*
     * Test for case of creating node which is inside choice-case statement
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCreateNode() throws DataStoreException, AnnotationAnalysisException, ModelNodeInitException {
        EntityDataStoreManager entityDSManager = mock(EntityDataStoreManager.class);
        Metamodel metaModel = mock(Metamodel.class);
        EntityType entity = mock(EntityType.class);
        Type type = mock(Type.class);
        when(m_persistenceManagerUtil.getEntityDataStoreManager()).thenReturn(entityDSManager);
        when(entityDSManager.getEntityManager()).thenReturn(mock(EntityManager.class));
        when(entityDSManager.getMetaModel()).thenReturn(metaModel);
        when(metaModel.entity(Song.class)).thenReturn(entity);
        when(metaModel.entity(Billboard.class)).thenReturn(entity);
        when(entity.getIdType()).thenReturn(type);
        when(type.getJavaType()).thenReturn(String.class);
        Billboard billboard = new Billboard();
        when(entityDSManager.findById(Billboard.class, "")).thenReturn(billboard);

        SchemaPath billboardSchemaPath = SchemaPath.create(true, BillboardConstants.BB_QNAME);
        SchemaPath choiceSchemaPath = new SchemaPathBuilder().withParent(billboardSchemaPath)
                .appendLocalName(BillboardConstants.ARWARD_CHOICE_LOCAL_NAME).build();
        SchemaPath songCaseSchemaPath = new SchemaPathBuilder().withParent(choiceSchemaPath)
                .appendLocalName(BillboardConstants.SINGLE_CASE_LOCAL_NAME).build();
        SchemaPath songSchemaPath = new SchemaPathBuilder().withParent(songCaseSchemaPath)
                .appendLocalName(BillboardConstants.SONG_LOCAL_NAME).build();
        QName songQname = QName.create(BillboardConstants.BB_NS, BillboardConstants.BB_REVISION, "name");

        ModelNodeWithAttributes billboardModelNode = new ModelNodeWithAttributes(billboardSchemaPath, null, m_modelNodeHelperRegistry,
                m_subSystemRegistry, m_schemaRegistry, m_dataStoreManager);
        billboardModelNode.setModelNodeId(TestConstants.EMPTY_NODE_ID);

        ModelNodeWithAttributes songModelNode = new ModelNodeWithAttributes(songSchemaPath, billboardModelNode.getModelNodeId(),
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_dataStoreManager);
        Map<QName, ConfigLeafAttribute> songAttrValues = new HashMap<>();
        songAttrValues.put(songQname, new GenericConfigAttribute("name", BillboardConstants.BB_NS, "The man who sold the world"));
        songModelNode.setAttributes(songAttrValues);

        ModelNodeId billboardParentId = billboardModelNode.getModelNodeId();
        ModelNode result = m_dataStoreManager.createNode(songModelNode, billboardParentId);
        assertNotNull(result);
        verify(entityDSManager).commitTransaction();
    }

}
