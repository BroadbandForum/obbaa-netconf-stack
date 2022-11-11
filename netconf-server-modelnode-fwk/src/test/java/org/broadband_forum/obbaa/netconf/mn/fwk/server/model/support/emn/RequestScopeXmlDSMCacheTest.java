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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class RequestScopeXmlDSMCacheTest {

    private XmlDSMCache m_cache;
    private XmlDSMCache m_cache2;
    @Mock
    private SchemaPath m_sp1;
    @Mock
    private ModelNodeId m_id1;
    @Mock
    private XmlModelNodeImpl m_node1;
    @Mock
    private XmlModelNodeImpl m_node2;
    @Mock
    private ModelNodeId m_id2;
    @Mock
    private SchemaPath m_sp2;

    private XmlDSMCache m_treeCache;
    @Mock
    private SchemaPath m_schemaPath;

    private ModelNodeId m_nodeId =  new ModelNodeId("/container=test1/container=test2", "http://test:ns");
    @Mock
    private XmlModelNodeImpl m_node;
    @Mock
    private SchemaPath m_schemaPath1;

    private ModelNodeId m_childNode1Id = new ModelNodeId("/container=test1/container=test2/container=test3", "http://test:ns");;
    @Mock
    private XmlModelNodeImpl m_childNode1;
    @Mock
    private SchemaPath m_schemaPath2;

    private ModelNodeId m_childNode2Id = new ModelNodeId("/container=test1/container=test2/container=test4", "http://test:ns");;
    @Mock
    private XmlModelNodeImpl m_childNode2;
    @Mock
    private SchemaPath m_schemaPath3;

    private ModelNodeId m_grandChildNode1Id = new ModelNodeId("/container=test1/container=test2/container=test4/container=test5", "http://test:ns");;
    @Mock
    private XmlModelNodeImpl m_grandChildNode1;
    @Mock
    private SchemaPath m_schemaPath4;

    private ModelNodeId m_grandChildNode2Id = new ModelNodeId("/container=test1/container=test2/container=test4/container=test6", "http://test:ns");;
    @Mock
    private XmlModelNodeImpl m_grandChildNode2;


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        m_cache = new RequestScopeXmlDSMCache();
        m_cache2 = new RequestScopeXmlDSMCache();
        m_treeCache = new RequestScopeXmlDSMCache();
    }

    @Test
    @Ignore("This test checks 'thread local-ness' of the class, " +
            "this is to be run manually every time the class is changed, not in CI")
    public void testCacheDoesNotInterfereWithMultipleThreads() throws InterruptedException {
        for(int i=0; i < 1000; i++){
            final ModelNode[] t1Node = new ModelNode[1];
            Thread t1 = new Thread(() -> {
                m_cache.putInCache(m_sp1, m_id1, m_node1);
                t1Node[0] = m_cache.getFromCache(m_sp1, m_id1);
            });
            final ModelNode[] t2Node = new ModelNode[1];
            Thread t2 = new Thread(() -> {
                m_cache.putInCache(m_sp1, m_id1, m_node2);
                t2Node[0] = m_cache.getFromCache(m_sp1, m_id1);
            });

            t1.start();
            t2.start();
            t1.join(500);
            t2.join(500);

            assertNotEquals(t1Node[0], t2Node[0]);
        }
    }

    @Test
    public void testCacheWorksForMultipleNodesOfDifferentTypes(){
        assertNull(m_cache.getFromCache(m_sp1, m_id1));
        assertNull(m_cache.getFromCache(m_sp2, m_id2));
        m_cache.putInCache(m_sp1, m_id1, m_node1);
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        assertNull(m_cache.getFromCache(m_sp2, m_id2));

        m_cache.putInCache(m_sp2, m_id2, m_node2);
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        assertEquals(m_node2, m_cache.getFromCache(m_sp2, m_id2));
    }

    @Test
    public void testCacheWorksForMultipleNodesOfSameType(){
        assertNull(m_cache.getFromCache(m_sp1, m_id1));
        assertNull(m_cache.getFromCache(m_sp1, m_id2));
        m_cache.putInCache(m_sp1, m_id1, m_node1);
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        assertNull(m_cache.getFromCache(m_sp1, m_id2));

        m_cache.putInCache(m_sp1, m_id2, m_node2);
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        assertEquals(m_node2, m_cache.getFromCache(m_sp1, m_id2));
    }

    @Test
    public void testMarkingNodesToBeUpdatedWorks(){
        m_cache.putInCache(m_sp1, m_id1, m_node1);
        m_cache.putInCache(m_sp1, m_id2, m_node2);
        m_cache.markNodeToBeUpdated(m_sp1, m_id1);
        verify(m_node1).toBeUpdated();
        verify(m_node2, never()).toBeUpdated();

        m_cache.markNodeToBeUpdated(m_sp1, m_id2);
        verify(m_node1).toBeUpdated();
        verify(m_node2).toBeUpdated();

        when(m_node1.isToBeUpdated()).thenReturn(true);
        when(m_node2.isToBeUpdated()).thenReturn(false);

        assertEquals(Arrays.asList(m_node1), m_cache.getNodesToBeUpdated());

        when(m_node2.isToBeUpdated()).thenReturn(true);
        assertEquals(new HashSet(Arrays.asList(m_node1, m_node2)), new HashSet(m_cache.getNodesToBeUpdated()));
    }

    @Test
    public void test2DifferentCachesDentInterfere(){
        m_cache.putInCache(m_sp1, m_id1, m_node1);
        assertNull(m_cache2.getFromCache(m_sp1, m_id1));
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        m_cache2.putInCache(m_sp1, m_id1, m_node2);
        assertEquals(m_node1, m_cache.getFromCache(m_sp1, m_id1));
        assertEquals(m_node2, m_cache2.getFromCache(m_sp1, m_id1));
    }

    @Test
    public void testRemoveFromCache() {
        m_treeCache.putInCache(m_schemaPath, m_nodeId, m_node);
        m_treeCache.putInCache(m_schemaPath1, m_childNode1Id, m_childNode1);
        m_treeCache.putInCache(m_schemaPath2, m_childNode2Id, m_childNode2);
        m_treeCache.putInCache(m_schemaPath3, m_grandChildNode1Id, m_grandChildNode1);
        m_treeCache.putInCache(m_schemaPath4, m_grandChildNode2Id, m_grandChildNode2);
        m_treeCache.removeFromCache(m_childNode2Id);
        assertNull(m_treeCache.getFromCache(m_schemaPath2, m_childNode2Id));
        assertNull(m_treeCache.getFromCache(m_schemaPath3, m_grandChildNode1Id));
        assertNull(m_treeCache.getFromCache(m_schemaPath4, m_grandChildNode2Id));
        assertEquals(m_childNode1, m_treeCache.getFromCache(m_schemaPath1, m_childNode1Id));
        assertEquals(m_node, m_treeCache.getFromCache(m_schemaPath,m_nodeId));
    }

    @Test
    public void testRemoveFromCacheScenario2() {
        m_treeCache.putInCache(m_schemaPath, m_nodeId, m_node);
        m_treeCache.putInCache(m_schemaPath1, m_childNode1Id, m_childNode1);
        m_treeCache.putInCache(m_schemaPath2, m_childNode2Id, m_childNode2);
        m_treeCache.putInCache(m_schemaPath3, m_grandChildNode1Id, m_grandChildNode1);
        m_treeCache.removeFromCache(m_grandChildNode2Id);
        assertEquals(m_childNode1,m_treeCache.getFromCache(m_schemaPath1, m_childNode1Id));
        assertEquals(m_childNode2,m_treeCache.getFromCache(m_schemaPath2, m_childNode2Id));
        assertEquals(m_grandChildNode1,m_treeCache.getFromCache(m_schemaPath3, m_grandChildNode1Id));
        assertNull(m_treeCache.getFromCache(m_schemaPath4, m_grandChildNode2Id));
    }

}
