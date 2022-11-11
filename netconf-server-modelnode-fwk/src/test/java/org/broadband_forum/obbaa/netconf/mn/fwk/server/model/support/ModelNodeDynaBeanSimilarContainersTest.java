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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.beanutils.DynaBean;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystemImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NbiNotificationHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.YangCopyConfigTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class ModelNodeDynaBeanSimilarContainersTest {

	private static final String ROOT_CONTAINER = "root-container";
	private static final String TEST = "test";
	private static final String SUBROOT = "subroot";
	private static final String COMMON_ROOT_YANG_REVISION = "2018-05-17";
	private static final String COMMON_ROOT_YANG_NS = "urn:opendaylight:commonroot";
	private static final String COMMON_ROOT_CHILD_TWO_NS = "urn:opendaylight:commonrootchildtwo";
	private static final String COMMON_ROOT_CHILD_ONE_NS = "urn:opendaylight:commonrootchildone";
	private static final String COMPONENT_ID = TEST;
	private static final String COMMON_ROOT_YANG_MODEL_PATH = YangCopyConfigTest.class
			.getResource("/referenceyangs/jukebox/commonroot.yang").getPath();

	private NetConfServerImpl m_server;
	private ModelNodeWithAttributes m_commonRootContainer;
	private SubSystemRegistry m_subSystemRegistry;

	private RootModelNodeAggregator m_runningRootModelNodeAggregator;
	private SchemaRegistry m_schemaRegistry;
	private NbiNotificationHelper m_nbiNotificationHelper = mock(NbiNotificationHelper.class);
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private ModelNodeDataStoreManager m_dataStoreManager;

	@SuppressWarnings("deprecation")
	@Before
	public void initServer() throws SchemaBuildException, ModelNodeInitException {

		m_schemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource> emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
		m_subSystemRegistry = new SubSystemRegistryImpl();
		m_subSystemRegistry.setCompositeSubSystem(new CompositeSubSystemImpl());
		m_server = new NetConfServerImpl(m_schemaRegistry);

		// build Schema Registry
		List<String> yangFiles = new ArrayList<>();
		yangFiles.add("/referenceyangs/jukebox/commonroot.yang");
		yangFiles.add("/referenceyangs/jukebox/commonrootchildone.yang");
		yangFiles.add("/referenceyangs/jukebox/commonrootchildtwo.yang");
		m_schemaRegistry.buildSchemaContext(TestUtil.getByteSources(yangFiles), Collections.emptySet(), Collections.emptyMap());

		m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
		m_dataStoreManager = new InMemoryDSM(m_schemaRegistry);
		m_commonRootContainer = YangUtils.createInMemoryModelNode(COMMON_ROOT_YANG_MODEL_PATH, new LocalSubSystem(),
				m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_dataStoreManager);
		m_runningRootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry,
				m_dataStoreManager, m_subSystemRegistry).addModelServiceRoot(COMPONENT_ID, m_commonRootContainer);
		addRegistryHelpers();

		DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_runningRootModelNodeAggregator,
				m_subSystemRegistry);
		dataStore.setNbiNotificationHelper(m_nbiNotificationHelper);
		m_server.setRunningDataStore(dataStore);
	}

	private void addRegistryHelpers() {
		// Register childContainerHelpers
		QName rootContainerQName = QName.create(COMMON_ROOT_YANG_NS, COMMON_ROOT_YANG_REVISION, ROOT_CONTAINER);
		SchemaPath rootSchemaPath = SchemaPath.create(true, rootContainerQName);
		QName commonRootChildOneQName = QName.create(COMMON_ROOT_CHILD_ONE_NS, COMMON_ROOT_YANG_REVISION, SUBROOT);
		SchemaPath subRootChildOne = new SchemaPathBuilder().withParent(rootSchemaPath)
				.appendQName(commonRootChildOneQName).build();
		ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(subRootChildOne);
		ChildContainerHelper containerHelper = new XmlContainerModelNodeHelper(schemaNode, m_dataStoreManager,
				m_schemaRegistry);
		m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_ID, rootSchemaPath, commonRootChildOneQName,
				containerHelper);

		QName commonRootChildTwoQName = QName.create(COMMON_ROOT_CHILD_TWO_NS, COMMON_ROOT_YANG_REVISION, SUBROOT);
		SchemaPath subRootChildTwo = new SchemaPathBuilder().withParent(rootSchemaPath)
				.appendQName(commonRootChildTwoQName).build();
		schemaNode = (ContainerSchemaNode) m_schemaRegistry.getDataSchemaNode(subRootChildTwo);
		containerHelper = new XmlContainerModelNodeHelper(schemaNode, m_dataStoreManager, m_schemaRegistry);

		m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_ID, rootSchemaPath, commonRootChildTwoQName,
				containerHelper);

		// InMemoryDSM
		createNodesInMemoryDSM(rootSchemaPath, subRootChildOne, subRootChildTwo);
	}

	private void createNodesInMemoryDSM(SchemaPath rootSchemaPath, SchemaPath subRootChildOne,
			SchemaPath subRootChildTwo) {

		// create root-container
		ModelNodeId rootContainerNodeId = new ModelNodeId()
				.addRdn(new ModelNodeRdn(CONTAINER, COMMON_ROOT_YANG_NS, ROOT_CONTAINER));
		ModelNodeWithAttributes rootContainerNode = new ModelNodeWithAttributes(rootSchemaPath, null, null, null,
				m_schemaRegistry, m_dataStoreManager);
		rootContainerNode.setModelNodeId(rootContainerNodeId);
		m_dataStoreManager.createNode(rootContainerNode, null);

		// create leaf in module one
		createChildContainersAndLeafs(subRootChildOne, rootContainerNodeId, COMMON_ROOT_CHILD_ONE_NS, "one");

		// create same leaf in module two
		createChildContainersAndLeafs(subRootChildTwo, rootContainerNodeId, COMMON_ROOT_CHILD_TWO_NS, "two");
	}

	private void createChildContainersAndLeafs(SchemaPath childSchemaPath, ModelNodeId rootContainerNodeId,
			String namespace, String leafValue) {
		SortedMap<QName, ConfigLeafAttribute> keys;
		ModelNodeWithAttributes subRootNode = new ModelNodeWithAttributes(childSchemaPath, rootContainerNodeId, null,
				null, m_schemaRegistry, m_dataStoreManager);
		ModelNodeId subrootId = new ModelNodeId(rootContainerNodeId)
				.addRdn(new ModelNodeRdn(CONTAINER, namespace, SUBROOT));
		subRootNode.setModelNodeId(subrootId);
		m_dataStoreManager.createNode(subRootNode, rootContainerNodeId);

		SchemaPath testNodeSchemaPath = new SchemaPathBuilder().withParent(childSchemaPath)
				.appendQName(QName.create(namespace, COMMON_ROOT_YANG_REVISION, TEST)).build();
		ModelNodeWithAttributes testNode = new ModelNodeWithAttributes(testNodeSchemaPath, subrootId, null, null,
				m_schemaRegistry, m_dataStoreManager);
		ModelNodeId testId = new ModelNodeId(subrootId).addRdn(new ModelNodeRdn(CONTAINER, namespace, TEST));
		testNode.setModelNodeId(testId);
		keys = new TreeMap<>();
		keys.put(QName.create(namespace, COMMON_ROOT_YANG_REVISION, TEST),
				new GenericConfigAttribute(TEST, namespace, leafValue));
		m_dataStoreManager.createNode(testNode, subrootId);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testDynaBean() throws ModelNodeInitException {
		DynaBean commonRootBean = ModelNodeDynaBeanFactory.getDynaBean(m_commonRootContainer);
		assertEquals(ROOT_CONTAINER, commonRootBean.getDynaClass().getName());

		List<Object> subRootObjs = (List<Object>) commonRootBean.get(SUBROOT);
		assertEquals(2, subRootObjs.size());
		List<String> namespaces = new ArrayList<String>();
		for (Object subRoot : subRootObjs) {
			DynaBean dynaBean = (DynaBean) subRoot;
			if (dynaBean.getDynaClass().getName().equals(SUBROOT)) {
				String namespace = (String) dynaBean.get(ModelNodeWithAttributes.NAMESPACE);
				namespaces.add(namespace);
			}
		}
		assertEquals(2, namespaces.size());
		assertTrue(namespaces.contains(COMMON_ROOT_CHILD_ONE_NS));
		assertTrue(namespaces.contains(COMMON_ROOT_CHILD_TWO_NS));
	}

}
