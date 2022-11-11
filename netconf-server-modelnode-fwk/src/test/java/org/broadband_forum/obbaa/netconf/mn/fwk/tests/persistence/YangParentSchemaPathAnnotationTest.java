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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.EMPTY_NODE_ID;
import static org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants.HOME_ADDRESS_NODE_ID;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDRESS_NAME_Q_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ADDR_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ALBUM_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.CELL_PHONE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_ADDRESS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_ADDRESSES_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_CELL;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_LAND_LINE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.HOME_TELPHONE_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LAND_LINE;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.MY_HOME_ADDRESS_KARNATAKA_560092;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.SONG_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.TELEPHONE_NUMBER_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.TELEPHONE_TYPE_QNAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.AnnotationBasedModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.TestTxUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.annotation.dao.JukeboxDao;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.SpyingThreadLocalPersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangparentschemapath.addresses.HomeAddress2;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangparentschemapath.addresses.TelephoneNumber2;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RunWith(RequestScopeJunitRunner.class)
public class YangParentSchemaPathAnnotationTest {
    private ModelNodeDataStoreManager m_dataStoreManager;
    private JukeboxDao m_jukeboxDao;
    private EntityRegistry m_entityRegistry;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private ModelNodeId m_jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JB_NS, JUKEBOX_LOCAL_NAME));
    private ModelNodeId m_libraryNodeId = new ModelNodeId(m_jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, LIBRARY_LOCAL_NAME));
    private final ModelNodeId m_artistId = new ModelNodeId(m_libraryNodeId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ARTIST_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "keshava"));
    private final ModelNodeId m_albumId = new ModelNodeId(m_artistId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, ALBUM_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "Refactor Times"));
    private final ModelNodeId m_songId = new ModelNodeId(m_albumId).addRdn(new ModelNodeRdn(CONTAINER, JB_NS, SONG_LOCAL_NAME))
            .addRdn(new ModelNodeRdn("name", JB_NS, "My Song"));

    private Jukebox m_newJukebox;
    private SpyingThreadLocalPersistenceManagerUtil m_persistenceManagerUtil;
    @Before
    public void setUp() throws AnnotationAnalysisException, SchemaBuildException, GetAttributeException {
        m_entityRegistry = new EntityRegistryImpl();
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();
        AbstractApplicationContext context = new ClassPathXmlApplicationContext("/modeldatastoremanagertest/test-applicationContext.xml");
        List<Class> classes = new ArrayList<>();
        classes.add(HomeAddress2.class);
        classes.add(TelephoneNumber2.class);
        List<YangTextSchemaSource> yangSources = TestUtil.getJukeBoxYangs();
        yangSources.addAll(TestUtil.getByteSources(Arrays.asList("/yangs/addresses@2015-12-08.yang")));
        m_schemaRegistry = new SchemaRegistryImpl(yangSources, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_persistenceManagerUtil = (SpyingThreadLocalPersistenceManagerUtil)context.getBean("persistenceManagerUtil");
        EntityRegistryBuilder.updateEntityRegistry("addresses", classes, m_entityRegistry, m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
        m_modelNodeHelperRegistry = mock(ModelNodeHelperRegistry.class);
        Map<QName, ConfigAttributeHelper> value = new LinkedHashMap<>();
        m_dataStoreManager = new AnnotationBasedModelNodeDataStoreManager(m_persistenceManagerUtil,m_entityRegistry, m_schemaRegistry, m_modelNodeHelperRegistry, null, m_modelNodeDSMRegistry);
        m_dataStoreManager = TestTxUtils.getTxDecoratedDSM(m_persistenceManagerUtil, m_dataStoreManager);
    }

    @Test
    public void testRemoveHomeAddressModel() throws DataStoreException, SchemaBuildException {
        List<YangTextSchemaSource> yangs = new ArrayList<>();
        yangs.add(TestUtil.getByteSource("/yangs/addresses@2015-12-08.yang"));
        m_schemaRegistry.loadSchemaContext("address-model", yangs, Collections.emptySet(), Collections.emptyMap());

        addHomeAddressesWithTelephones();
        assertEquals(2, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH, m_schemaRegistry).size());
        //Remove one telephone number from home address
        ModelNodeWithAttributes telephoneNumberModelNode = new ModelNodeWithAttributes(HOME_TELPHONE_SCHEMA_PATH, null, null, null,
                m_schemaRegistry, m_dataStoreManager);
        Map<QName,ConfigLeafAttribute> configAttributes  = new HashMap<>();
        configAttributes.put(TELEPHONE_TYPE_QNAME, new GenericConfigAttribute("type", ADDR_NS, LAND_LINE));
        configAttributes.put(TELEPHONE_NUMBER_QNAME, new GenericConfigAttribute("number", ADDR_NS, HOME_LAND_LINE));
        telephoneNumberModelNode.setAttributes(configAttributes);
        telephoneNumberModelNode.setModelNodeId(new ModelNodeId("/container=telephone-number", ADDR_NS));
        m_dataStoreManager.removeNode(telephoneNumberModelNode, HOME_ADDRESS_NODE_ID);
        assertEquals(1, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH, m_schemaRegistry).size());
        verify(m_persistenceManagerUtil.getSpy(), never()).findById(eq(HomeAddress2.class), anyObject(), anyObject());
    }

    @Test
    public void testCreateModelNodeHomeAddressModel() throws DataStoreException, SchemaBuildException {
        addHomeAddress();
        //Add one telephone number to existing home address
        assertEquals(0, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH, m_schemaRegistry).size());

        ModelNodeWithAttributes telephoneNumberModelNode = new ModelNodeWithAttributes(HOME_TELPHONE_SCHEMA_PATH, null, null, null, m_schemaRegistry, m_dataStoreManager);
        Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<>();
        configAttributes.put(TELEPHONE_TYPE_QNAME, new GenericConfigAttribute("type", ADDR_NS, LAND_LINE));
        configAttributes.put(TELEPHONE_NUMBER_QNAME, new GenericConfigAttribute("number", ADDR_NS, "someNumber"));
        telephoneNumberModelNode.setAttributes(configAttributes);
        ModelNodeId homeAddressNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, ADDR_NS, "home-address")).addRdn(new
                ModelNodeRdn(ADDRESS_NAME_Q_NAME, "new-home"));

        m_dataStoreManager.createNode(telephoneNumberModelNode, homeAddressNodeId);
        assertEquals(1, m_dataStoreManager.listNodes(HOME_TELPHONE_SCHEMA_PATH, m_schemaRegistry).size());
        verify(m_persistenceManagerUtil.getSpy(), never()).findById(eq(HomeAddress2.class), anyObject(), anyObject());
    }

    private void addHomeAddressesWithTelephones() {
        addHomeAddress();
        EntityDataStoreManager enityDataStoreManager;
        TelephoneNumber2 landline = new TelephoneNumber2();
        landline.setSchemaPath(SchemaPathUtil.toStringNoRev(HOME_TELPHONE_SCHEMA_PATH));
        landline.setNumber(HOME_LAND_LINE);
        landline.setParentId(HOME_ADDRESS_NODE_ID.getModelNodeIdAsString());
        landline.setType(LAND_LINE);

        //Seriously ? cell phone for office ? :D
        TelephoneNumber2 cellPhone = new TelephoneNumber2();
        cellPhone.setSchemaPath(SchemaPathUtil.toStringNoRev(HOME_TELPHONE_SCHEMA_PATH));
        cellPhone.setType(CELL_PHONE);
        cellPhone.setParentId(HOME_ADDRESS_NODE_ID.getModelNodeIdAsString());
        cellPhone.setNumber(HOME_CELL);

        enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(landline);
            enityDataStoreManager.create(cellPhone);
            enityDataStoreManager.commitTransaction();
        }catch (Exception e){
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }

    private void addHomeAddress() {
        HomeAddress2 homeAddress = new HomeAddress2();
        homeAddress.setSchemaPath(SchemaPathUtil.toStringNoRev(HOME_ADDRESSES_SCHEMA_PATH));
        homeAddress.setAddressName(HOME_ADDRESS);
        homeAddress.setAddress(MY_HOME_ADDRESS_KARNATAKA_560092);
        homeAddress.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());

        EntityDataStoreManager enityDataStoreManager = m_persistenceManagerUtil.getEntityDataStoreManager();
        enityDataStoreManager.beginTransaction();
        try {
            enityDataStoreManager.create(homeAddress);
            enityDataStoreManager.commitTransaction();
        }catch (Exception e){
            enityDataStoreManager.rollbackTransaction();
            throw e;
        }
    }
}
