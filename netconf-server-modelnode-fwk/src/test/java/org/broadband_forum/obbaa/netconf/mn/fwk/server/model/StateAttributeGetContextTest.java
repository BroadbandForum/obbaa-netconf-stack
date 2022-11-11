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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

public class StateAttributeGetContextTest {

    private StateAttributeGetContext m_stateAttributeGetContext;
    private Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> m_attributes;
    private SubSystem m_subSystem;
    private Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> m_stateResponseNotInCache;
    private Map<ModelNodeId, List<Element>> m_returnValues;
    private ModelNodeId m_mnId1;
    private ModelNodeId m_mnId2;
    private QName m_qName1;
    private QName m_qName2;
    private FilterNode m_filterNode1;
    private FilterNode m_filterNode2;

    @Before
    public void setup(){
        m_stateAttributeGetContext = new StateAttributeGetContext();
        m_attributes = new TreeMap<>();
        m_stateResponseNotInCache = new HashMap<>();
        m_returnValues = new HashMap<>();

        m_qName1 = QName.create("ns","qName1");
        m_qName2 = QName.create("ns", "qName2");
        m_filterNode1 = new FilterNode("filter1","ns");
        m_filterNode2 = new FilterNode("filter2","ns");

        m_subSystem = mock(SubSystem.class);
        m_mnId1 = mock(ModelNodeId.class);
        m_mnId2 = mock(ModelNodeId.class);
    }

    @Test
    public void testFindAndPopulateStoredLeavesForSS_NoCacheHits(){
        populateStateLeaves(m_attributes, m_mnId1, m_qName1, m_mnId2, m_qName2);

        assertEquals(0, m_stateResponseNotInCache.size());
        assertEquals(0, m_returnValues.size());
        m_stateAttributeGetContext.findAndPopulateStoredLeavesForSS(m_returnValues, m_stateResponseNotInCache, m_attributes, m_subSystem);
        assertEquals(m_attributes, m_stateResponseNotInCache);
        assertEquals(2, m_stateResponseNotInCache.size());
    }

    @Test
    public void testFindAndPopulateStoredLeavesForSS_CacheHits(){
        populateStateLeaves(m_attributes,m_mnId1,m_qName1,m_mnId2,m_qName2);

        StoredLeaves storedLeaves = m_stateAttributeGetContext.getStoredLeavesForSS(m_subSystem);
        Element storedLeaf = mock(Element.class);
        storedLeaves.put(m_mnId1,m_qName1, storedLeaf);
        assertEquals(0, m_stateResponseNotInCache.size());
        assertEquals(0, m_returnValues.size());
        m_stateAttributeGetContext.findAndPopulateStoredLeavesForSS(m_returnValues, m_stateResponseNotInCache, m_attributes, m_subSystem);

        // verify one leaf is not in cache
        assertEquals(1, m_stateResponseNotInCache.size());
        assertEquals(m_mnId2, m_stateResponseNotInCache.keySet().iterator().next());
        assertEquals(1, m_stateResponseNotInCache.get(m_mnId2).getFirst().size());
        assertEquals(m_qName2, m_stateResponseNotInCache.get(m_mnId2).getFirst().get(0));

        // verify leaf in cache is added to returnValues
        assertEquals(1, m_returnValues.size());
        assertEquals(m_mnId1, m_returnValues.keySet().iterator().next());
        assertEquals(1, m_returnValues.get(m_mnId1).size());
        assertEquals(storedLeaf, m_returnValues.get(m_mnId1).get(0));
    }

    @Test
    public void testFindAndPopulateStoredFiltersForSS_NoCacheHits(){
        populateStateFilters(m_attributes, m_mnId1, m_filterNode1, m_mnId2, m_filterNode2);

        assertEquals(0, m_stateResponseNotInCache.size());
        assertEquals(0, m_returnValues.size());
        m_stateAttributeGetContext.findAndPopulateStoredFiltersForSS(m_returnValues, m_stateResponseNotInCache, m_attributes, m_subSystem);
        assertEquals(m_attributes, m_stateResponseNotInCache);
        assertEquals(2, m_stateResponseNotInCache.size());
    }

    @Test
    public void testFindAndPopulateStoredFiltersForSS_CacheHits(){
        populateStateFilters(m_attributes,m_mnId1, m_filterNode1,m_mnId2, m_filterNode2);

        StoredFilters storedFilterNodes = m_stateAttributeGetContext.getStoredFilterNodesForSS(m_subSystem);
        Element storedFilterResponse = mock(Element.class);
        storedFilterNodes.put(m_mnId1, m_filterNode1, storedFilterResponse);
        assertEquals(0, m_stateResponseNotInCache.size());
        assertEquals(0, m_returnValues.size());
        m_stateAttributeGetContext.findAndPopulateStoredFiltersForSS(m_returnValues, m_stateResponseNotInCache, m_attributes, m_subSystem);

        // verify one leaf is not in cache
        assertEquals(1, m_stateResponseNotInCache.size());
        assertEquals(m_mnId2, m_stateResponseNotInCache.keySet().iterator().next());
        assertEquals(1, m_stateResponseNotInCache.get(m_mnId2).getSecond().size());
        assertEquals(m_filterNode2, m_stateResponseNotInCache.get(m_mnId2).getSecond().get(0));

        // verify leaf in cache is added to returnValues
        assertEquals(1, m_returnValues.size());
        assertEquals(m_mnId1, m_returnValues.keySet().iterator().next());
        assertEquals(1, m_returnValues.get(m_mnId1).size());
        assertEquals(storedFilterResponse, m_returnValues.get(m_mnId1).get(0));
    }

    @Test
    public void testCacheStateleaves(){

        Element stateLeaf1 = mock(Element.class);
        when(stateLeaf1.getNamespaceURI()).thenReturn("ns");
        when(stateLeaf1.getLocalName()).thenReturn("qName1");

        Element stateLeaf2 = mock(Element.class);
        when(stateLeaf2.getNamespaceURI()).thenReturn("ns");
        when(stateLeaf2.getLocalName()).thenReturn("qName2");

        List<Element> stateElementsForMnId1 = new ArrayList<>();
        stateElementsForMnId1.add(stateLeaf1);

        List<Element> stateElementsForMnId2 = new ArrayList<>();
        stateElementsForMnId2.add(stateLeaf2);

        Map<ModelNodeId, List<Element>> stateValues = new HashMap<>();
        stateValues.put(m_mnId1, stateElementsForMnId1);
        stateValues.put(m_mnId2, stateElementsForMnId2);

        populateStateLeaves(m_stateResponseNotInCache, m_mnId1, m_qName1, m_mnId2,m_qName2);

        StoredLeaves storedLeaves = m_stateAttributeGetContext.getStoredLeavesForSS(m_subSystem);
        assertEquals(0, storedLeaves.get(m_mnId1).size());
        assertEquals(0, storedLeaves.get(m_mnId2).size());

        m_stateAttributeGetContext.cacheStateResponse(stateValues,m_stateResponseNotInCache,m_subSystem);

        assertEquals(1, storedLeaves.get(m_mnId1).size());
        assertEquals(m_qName1, storedLeaves.get(m_mnId1).keySet().iterator().next());
        assertEquals(stateLeaf1, storedLeaves.get(m_mnId1).get(m_qName1));

        assertEquals(1, storedLeaves.get(m_mnId2).size());
        assertEquals(m_qName2, storedLeaves.get(m_mnId2).keySet().iterator().next());
        assertEquals(stateLeaf2, storedLeaves.get(m_mnId2).get(m_qName2));
    }

    @Test
    public void testCacheStateFilters(){

        Element stateElement1 = mock(Element.class);
        when(stateElement1.getNamespaceURI()).thenReturn("ns");
        when(stateElement1.getLocalName()).thenReturn("filter1");

        Element stateElement2 = mock(Element.class);
        when(stateElement2.getNamespaceURI()).thenReturn("ns");
        when(stateElement2.getLocalName()).thenReturn("filter2");

        List<Element> stateElementsForMnId1 = new ArrayList<>();
        stateElementsForMnId1.add(stateElement1);

        List<Element> stateElementsForMnId2 = new ArrayList<>();
        stateElementsForMnId2.add(stateElement2);

        Map<ModelNodeId, List<Element>> stateValues = new HashMap<>();
        stateValues.put(m_mnId1, stateElementsForMnId1);
        stateValues.put(m_mnId2, stateElementsForMnId2);

        populateStateFilters(m_stateResponseNotInCache,m_mnId1,m_filterNode1,m_mnId2,m_filterNode2);

        StoredFilters storedFilterNodes = m_stateAttributeGetContext.getStoredFilterNodesForSS(m_subSystem);
        assertEquals(0, storedFilterNodes.get(m_mnId1).size());
        assertEquals(0, storedFilterNodes.get(m_mnId2).size());

        m_stateAttributeGetContext.cacheStateResponse(stateValues,m_stateResponseNotInCache,m_subSystem);

        assertEquals(1, storedFilterNodes.get(m_mnId1).size());
        assertEquals(m_filterNode1, storedFilterNodes.get(m_mnId1).keySet().iterator().next());
        assertEquals(stateElement1, storedFilterNodes.get(m_mnId1).get(m_filterNode1).get(0));

        assertEquals(1, storedFilterNodes.get(m_mnId2).size());
        assertEquals(m_filterNode2, storedFilterNodes.get(m_mnId2).keySet().iterator().next());
        assertEquals(stateElement2, storedFilterNodes.get(m_mnId2).get(m_filterNode2).get(0));

    }

    private void populateStateFilters(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                      ModelNodeId mnId1, FilterNode filterNode1, ModelNodeId mnId2, FilterNode filterNode2) {
        List<FilterNode> filterNodes1 = new ArrayList<>();
        filterNodes1.add(filterNode1);
        attributes.put(mnId1, new Pair<>(Collections.emptyList(),filterNodes1));

        List<FilterNode> filterNodes2 = new ArrayList<>();
        filterNodes2.add(filterNode2);
        attributes.put(mnId2, new Pair<>(Collections.emptyList(),filterNodes2));
    }

    private void populateStateLeaves(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes,
                                     ModelNodeId mnId1, QName qName1, ModelNodeId mnId2, QName qName2) {
        List<QName> leavesForMnId1 = new ArrayList<>();
        leavesForMnId1.add(qName1);
        attributes.put(mnId1, new Pair<>(leavesForMnId1,Collections.emptyList()));

        List<QName> leavesForMnId2 = new ArrayList<>();
        leavesForMnId2.add(qName2);
        attributes.put(mnId2, new Pair<>(leavesForMnId2,Collections.emptyList()));
    }


}
