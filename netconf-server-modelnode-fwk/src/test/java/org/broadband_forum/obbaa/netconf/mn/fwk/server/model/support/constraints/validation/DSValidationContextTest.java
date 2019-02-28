package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public class DSValidationContextTest {

    DSValidationContext m_dsValidationContext;

    @Mock
    RootModelNodeAggregator m_rootModelNodeAggregator;
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
        assertEquals(m_modelNodeList, m_dsValidationContext.getRootNodesOfType("www.example.com", "test"));
        verify(m_rootModelNodeAggregator).getModuleRootFromHelpers("www.example.com", "test");

        assertEquals(m_modelNodeList, m_dsValidationContext.getRootNodesOfType("www.example.com", "test"));
        verify(m_rootModelNodeAggregator).getModuleRootFromHelpers("www.example.com", "test");
    }
    
    @Test
    public void testNodesToCreateCount() throws Exception{
    	m_dsValidationContext.nodesToCreateCount();
    }
    
    @Test
    public void testAddToModelNodeCache() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
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
    	QName child = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForDelete(modelNode, child);
    }
    
    @Test
    public void testRecordForDeleteSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recordForDelete(schemaPath);
    }
    
    @Test
    public void testRecordForCreate() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	QName child = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForCreate(modelNode, child);
    }
    
    @Test
    public void testRecordForCreateSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recrodForCreate(schemaPath);
    }
    
    @Test
    public void testRecordForMerge() throws Exception{
    	ModelNode modelNode = Mockito.mock(ModelNode.class);
    	QName child = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	m_dsValidationContext.recordForMerge(modelNode, child);
    }
    
    @Test
    public void testRecordForCreteSchemaPath() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
    	SchemaPath schemaPath = SchemaPath.create(true, VALIDATION_QNAME);
    	m_dsValidationContext.recordForDelete(schemaPath);
    }
    
    @Test
    public void testRecordDefaultValue() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
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
    	m_dsValidationContext.recrodDeletedChangeAttribute(null, null);
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
    	m_dsValidationContext.getDocument();
    }
    
    @Test
    public void testGetSchemaPathsToDelete() throws Exception{
    	m_dsValidationContext.getSchemaPathsToDelete();
    }
    
    @Test
    public void testIsNodeForCreate() throws Exception{
    	QName VALIDATION_QNAME = QName.create("urn:org:bbf:pma:validation", "2015-12-14", "validation");
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
    	m_dsValidationContext.cacheRootNodes("urn:org:bbf:pma:validation", "validation", rootNodes);
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
    	m_dsValidationContext.getAugmentChildNode();
    }
    
    @Test
    public void testSetAugmentChildNode() throws Exception{
    	m_dsValidationContext.setAugmentChildNode(null);
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
    public void testGetRootNodeLocalNameNsPairs() throws Exception{
    	m_dsValidationContext.getRootNodeLocalNameNsPairs();
    }
    
}