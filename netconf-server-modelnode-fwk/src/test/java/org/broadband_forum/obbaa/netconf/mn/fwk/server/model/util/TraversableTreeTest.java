package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TraversableTreeTest {

    private TraversableTree<QName> m_testObj = null;

    QName m_child1 = QName.create("global", "child1");
    QName m_child12 = QName.create("global", "child12");
    QName m_child123 = QName.create("global", "child123");
    QName m_child2 = QName.create("global", "child2");

    @Before
    public void setUp() {
        m_testObj = new TraversableTree<>();
    }

    @After
    public void tearDown() {
        m_testObj = null;
    }

    @Test
    public void testConstruction() {
        assertNotNull(m_testObj);
    }

    @Test
    public void testInsertionAtRoot() {
        m_testObj.insertNodeAt(null, m_child1);
        m_testObj.insertNodeAt(null, m_child2);
        SortedSet<QName> result = m_testObj.getAllChildrenOf(null);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(m_child1, result.first());
        assertEquals(m_child2, result.last());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInsertionFailureDuplicate() {
        testInsertionAtRoot();
        m_testObj.insertNodeAt(null, m_child1);
    }

    @Test
    public void testInsertionAtNonRootLevel1() {
        testInsertionAtRoot();
        m_testObj.insertNodeAt(Arrays.asList(m_child1), m_child12);
        SortedSet<QName> result = m_testObj.getAllChildrenOf(Arrays
                .asList(m_child1));
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(m_child12, result.iterator().next());
    }

    @Test
    public void testInsertionAtNonRootLevel2() {
        testInsertionAtNonRootLevel1();
        m_testObj.insertNodeAt(Arrays.asList(m_child1, m_child12), m_child123);
        SortedSet<QName> result = m_testObj.getAllChildrenOf(Arrays
                .asList(m_child1));
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(m_child12, result.first());
        assertEquals(m_child123, result.last());
    }

    @Test(expected = IllegalStateException.class)
    public void testInsertionFailureNoPath() {
        testInsertionAtNonRootLevel1();
        m_testObj.insertNodeAt(Arrays.asList(m_child12), m_child123);
    }

    @Test
    public void testRemoveFromRoot() {
        testInsertionAtRoot();
        m_testObj.removeNode(m_child1);
        SortedSet<QName> result = m_testObj.getAllChildrenOf(null);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void testRemoveFromNonRoot() {
        testInsertionAtNonRootLevel1();
        m_testObj.removeNode(m_child12);
        SortedSet<QName> result = m_testObj.getAllChildrenOf(Arrays
                .asList(m_child1));
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testFindPathTo() {
        testInsertionAtNonRootLevel2();
        List<QName> path = m_testObj.findPathTo(m_child123);
        assertNotNull(path);
        assertEquals(2, path.size());
        assertEquals(m_child1, path.get(0));
        assertEquals(m_child12, path.get(1));

        SortedSet<QName> children = m_testObj.getAllChildrenOf(path);
        assertNotNull(children);
        assertEquals(1, children.size());
        assertEquals(m_child123, children.first());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindPathToFailureByNull() {
        m_testObj.findPathTo(null);
    }

    @Test
    public void testFindPathToNotFoundScenario() {
        assertNull(m_testObj.findPathTo(m_child123));
    }

    @Test
    public void testGetAllChildren() {
        testInsertionAtNonRootLevel1();
        SortedSet<QName> result = m_testObj.getAllChildrenOf(null);
        assertNotNull(result);
        assertEquals(3, result.size());
    }

}
