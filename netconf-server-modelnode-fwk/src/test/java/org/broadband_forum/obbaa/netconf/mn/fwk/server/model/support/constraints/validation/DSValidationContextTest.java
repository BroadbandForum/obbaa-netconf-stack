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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountRegistries;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DSValidationContextTest {

    DSValidationContext m_dsValidationContext;

    @Mock RootModelNodeAggregator m_rootModelNodeAggregator;
    @Mock SchemaRegistry m_schemaRegistry;
    private SchemaPath m_path = SchemaPath.create(true, QName.create("www.example.com", "2018-01-01", "test"));
    private List<ModelNode> m_modelNodeList;

	Map<SchemaPath, Map<String, ModelNode>> m_modelNodeCache;


    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
        m_dsValidationContext = new DSValidationContext();
        m_dsValidationContext.setRootNodeAggregator(m_rootModelNodeAggregator);
        m_modelNodeList = new ArrayList<ModelNode>();
        when(m_rootModelNodeAggregator.getModuleRootFromHelpers("www.example.com", "test")).thenReturn(m_modelNodeList);
    }

    @Test
    public void testGetRootNodesOfType(){
        verifyZeroInteractions(m_rootModelNodeAggregator);
        assertEquals(m_modelNodeList, m_dsValidationContext.getRootNodesOfType(m_path, m_schemaRegistry));
        verify(m_rootModelNodeAggregator).getModuleRootFromHelpers("www.example.com", "test");

        assertEquals(m_modelNodeList, m_dsValidationContext.getRootNodesOfType(m_path, m_schemaRegistry));
        verify(m_rootModelNodeAggregator).getModuleRootFromHelpers("www.example.com", "test");
    }

    @Test
    public void testNodesToCreateCount() throws Exception{
    	m_dsValidationContext.nodesToCreateCount();
    }

    @Test
    public void testAddToModelNodeCache() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
		ModelNode modelNode = Mockito.mock(ModelNode.class);
		ModelNode modelNode2 = Mockito.mock(ModelNode.class);
		ModelNodeId modelNodeId = Mockito.mock(ModelNodeId.class);
		Map<String, ModelNode> valueMap = new HashMap<>();
		valueMap.put("12", modelNode2);
		m_dsValidationContext.setModeNodeCache(schemaPath, valueMap);
		when(modelNode.getModelNodeId()).thenReturn(modelNodeId);
		m_dsValidationContext.addToModelNodeCache(schemaPath, modelNode);

		m_dsValidationContext.addToModelNodeCache(null, modelNode);
    }

    @Test
    public void testRemoveFromDelete() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	m_dsValidationContext.removeFromDelete(modelNode);
    }

    @Test
    public void testRemoveFromMerge() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	m_dsValidationContext.removeFromMerge(modelNode);
    }

    @Test
    public void testRemoveFromCreate() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	m_dsValidationContext.removeFromCreate(modelNode);
    }

    @Test
    public void testRecordForDelete() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	QName child = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForDelete(modelNode, child);
    }

    @Test
    public void testRecordForDeleteSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recordForDelete(schemaPath);
    }

    @Test
    public void testRecordForCreate() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	QName child = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForCreate(modelNode, child);
    }

    @Test
    public void testRecordForCreateSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recordForCreate(schemaPath);
    }

    @Test
    public void testRecordForMerge() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	QName child = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForMerge(modelNode, child);
    }

    @Test
    public void testRecordForCreteSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recordForDelete(schemaPath);
    }

    @Test
    public void testRecordDefaultValue() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
        ModelNode modelNode = Mockito.mock(ModelNode.class);
        ModelNodeId modelNodeId = Mockito.mock(ModelNodeId.class);
        when(modelNode.getModelNodeId()).thenReturn(modelNodeId);
    	m_dsValidationContext.recordDefaultValue(schemaPath, modelNode, null);
    }

    @Test
    public void testRecordDeletedChangeNode() throws Exception{
    	m_dsValidationContext.recordDeletedChangeNode(null, null);
    }

    @Test
    public void testRecrodDeletedChangeAttribute() throws Exception{
    	m_dsValidationContext.recordDeletedChangeAttribute(null, null);
    }

    @Test
    public void testGetModelNodes() throws Exception{
    	m_dsValidationContext.getModelNodes(null);
    }

    @Test
    public void testGetDefaultValues() throws Exception{
    	m_dsValidationContext.getDefaultValues();
    }

    @Test
    public void testGetSchemaPathsToCreate() throws Exception{
    	m_dsValidationContext.getSchemaPathsToCreate();
    }

    @Test
    public void testGetDocument() throws Exception{
        assertNotNull(m_dsValidationContext.getDocument());
    }

    @Test
    public void testGetSchemaPathsToDelete() throws Exception{
    	m_dsValidationContext.getSchemaPathsToDelete();
    }

    @Test
    public void testIsNodeForCreate() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.isNodeForCreate(schemaPath);
    }

    @Test
    public void testIsNodeForCreateSchemaPath() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	m_dsValidationContext.isNodeForCreate(modelNode);
    }

    @Test
    public void testIsNodeForDelete() throws Exception{
    	m_dsValidationContext.isNodeForDelete(null);
    }

    @Test
    public void testIsNodeForCreateOrDelete() throws Exception{
    	m_dsValidationContext.isNodeForCreateOrDelete(null);
    }

    @Test
    public void testGetDeletedNode() throws Exception{
    	m_dsValidationContext.getDeletedNode(null);
    }

    @Test
    public void testGetDeletedAttribute() throws Exception{
    	m_dsValidationContext.getDeletedAttribute(null);
    }

    @Test
    public void testGetValidationNode() throws Exception{
    	m_dsValidationContext.getValidationNode();
    }

    @Test
    public void testSetValidationNode() throws Exception{
    	m_dsValidationContext.setValidationNode(null);
    }

    @Test
    public void testCacheRootNodes() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	Collection<ModelNode> rootNodes = new HashSet<>();
    	rootNodes.add(modelNode);
    	m_dsValidationContext.cacheRootNodesWithSchemaRegistry("urn:org:bbf2:pma:validation", "validation", rootNodes, m_schemaRegistry.getName());
    }

    @Test
    public void testGetDeleteList() throws Exception{
    	m_dsValidationContext.getDeleteList();
    }

    @Test
    public void testGetCreateList() throws Exception{
    	m_dsValidationContext.getCreateList();
    }

    @Test
    public void testGetMergeList() throws Exception{
    	m_dsValidationContext.getMergeList();
    }

    @Test
    public void testGetChildOfChoiceCase() throws Exception{
    	m_dsValidationContext.getChildOfChoiceCase();
    }

    @Test
    public void testSetChildOfChoiceCase() throws Exception{
    	m_dsValidationContext.setChildOfChoiceCase(null);
    }

    @Test
    public void testGetAugmentChildNode() throws Exception{
    	m_dsValidationContext.getAugmentOrUsesChildNode();
    }

    @Test
    public void testSetAugmentChildNode() throws Exception{
    	m_dsValidationContext.setAugmentOrUsesChildNode(null);
    }

    @Test
    public void testGetImpactValidation() throws Exception{
    	m_dsValidationContext.getImpactValidation();
    }

    @Test
    public void testSetImpactValidation() throws Exception{
    	m_dsValidationContext.setImpactValidation(true);
    }

    @Test
    public void getRootNodeSchemaPaths() throws Exception{
    	m_dsValidationContext.getRootNodeSchemaPaths();
    }

    @Test
    public void getRootNodesOfType(){
    	String ns = "www.test.com";
		QName qname1 = QName.create(ns, "firstContainer");
		QName qname2 = QName.create(ns, "secondContainer");
		QName qname3 = QName.create(ns, "thirdContainer");
		QName qname4 = QName.create(ns, "fourthContainer");
		SchemaPath schemaPath = SchemaPath.create(true, qname1, qname2, qname3);
		SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
		SchemaRegistry globalRegistry = mock(SchemaRegistry.class);
		SchemaMountRegistry mountRegistry = mock(SchemaMountRegistry.class);
		SchemaMountRegistryProvider provider = mock(SchemaMountRegistryProvider.class);
		RootModelNodeAggregator aggregator = mock(RootModelNodeAggregator.class);
		RootModelNodeAggregator mountedAggregator = mock(RootModelNodeAggregator.class);
		when(schemaRegistry.getName()).thenReturn("G.FAST|1.0");
		when(schemaRegistry.getParentRegistry()).thenReturn(globalRegistry);
		when(globalRegistry.getMountRegistry()).thenReturn(mountRegistry);
		when(mountRegistry.getProvider(schemaPath)).thenReturn(provider);
		MountRegistries mountRegistries = new MountRegistries(null);
		mountRegistries.setRootModelNodeAggregator(mountedAggregator);
		when(provider.getMountRegistries("G.FAST|1.0")).thenReturn(mountRegistries );
		List<ModelNodeRdn> rdns = new ArrayList<>();
		ModelNodeRdn rdn1 = new ModelNodeRdn(qname1, null);
		ModelNodeRdn rdn2 = new ModelNodeRdn(qname2, "device2");
		ModelNodeRdn rdn3 = new ModelNodeRdn(qname3, null);
		ModelNodeRdn rdn4 = new ModelNodeRdn(qname4, "interface");
		rdns.add(rdn1);
		rdns.add(rdn2);
		rdns.add(rdn3);
		rdns.add(rdn4);
		ModelNodeId currentId = new ModelNodeId(rdns);
		ModelNodeId gfastId = getModelNodeId("device2");
		ModelNodeId sxId = getModelNodeId("device1");
		ModelNode gfastNode = mock(ModelNode.class);
		ModelNode sxNode = mock(ModelNode.class);
		ModelNode interfaceNode = mock(ModelNode.class);
		when(gfastNode.getModelNodeId()).thenReturn(gfastId);
		when(sxNode.getModelNodeId()).thenReturn(sxId);
		Map<String, ModelNode> valueMap = new HashMap<>();
		valueMap.put(sxId.toString(), sxNode);
		valueMap.put(gfastId.toString(), gfastNode);
		when(schemaRegistry.getMountPath()).thenReturn(schemaPath);
		when(mountedAggregator.getModuleRootFromHelpers(ns, "thirdContainer", gfastNode)).thenReturn(Arrays.asList(interfaceNode));
		m_dsValidationContext.setModeNodeCache(schemaPath, valueMap );
		m_dsValidationContext.setCurrentModelNodeId(currentId);
		m_dsValidationContext.setRootNodeAggregator(aggregator);
		List<ModelNode> result = m_dsValidationContext.getRootNodesOfType(schemaPath, schemaRegistry);
		assertEquals(Arrays.asList(interfaceNode), result);
    }

    private ModelNodeId getModelNodeId(String value){
    	QName qname1 = QName.create("www.test.com", "firstContainer");
		QName qname2 = QName.create("www.test.com", "secondContainer");
		QName qname3 = QName.create("www.test.com", "thirdContainer");
		List<ModelNodeRdn> rdns = new ArrayList<>();
    	ModelNodeRdn rdn1 = new ModelNodeRdn(qname1, null);
		ModelNodeRdn rdn2 = new ModelNodeRdn(qname2, value);
		ModelNodeRdn rdn3 = new ModelNodeRdn(qname3, null);
		rdns.add(rdn1);
		rdns.add(rdn2);
		rdns.add(rdn3);
		return new ModelNodeId(rdns );
    }
}