package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;

public class DefaultConcurrentHashMapTest {

    @Test
    public void test() {

        Map<String, Map<?, ?>> map1 = new DefaultConcurrentHashMap<>(null);
        assertNull(map1.get(new Object()));

        Map<String, TreeMap<String, String>> map2 = new DefaultConcurrentHashMap<>(new TreeMap<String, String>());
        Map<String, String> childMap = map2.get("Hello");
        assertNotNull(childMap);
        assertTrue(childMap instanceof TreeMap);
        childMap.put("1", "2");
        childMap = map2.get("Hello");
        assertTrue(childMap.containsKey("1"));

        Map<String, String> mapA = map2.get("a");
        mapA.put("a", "b");
        Map<String, String> mapB = map2.get("b");
        assertTrue(mapB.size() == 0);
    }
    
    @Test
    public void testCustomObject() {
        Map<String, TestClass> testObjectMap = new DefaultConcurrentHashMap<>(new TestClass());
        assertNotNull(testObjectMap.get("oneString"));
        assertNull(testObjectMap.get(null));
    }
    
    static class TestClass {
        private String m_oneString;
        
        public String getOneString() { return m_oneString;}
        public void setOneString(String oneString) { m_oneString = oneString;}
    }

    @Test
    public void testNullKey() {
        Map<String, Integer> map1 = new DefaultConcurrentHashMap<>(123);
        assertNotNull(map1.get(123));
        assertNull(map1.get(null));
    }

    @Test
    public void testSynchornizedMap() {
        Map<String, HashMap<String, String>> map1 = new DefaultConcurrentHashMap<>(new HashMap<String, String>(), true);
        Map<String, String> object1 = map1.get("1");
        assertNotNull(object1);
        assertEquals("java.util.Collections$SynchronizedMap", object1.getClass().getName());

        object1.put("a", "b");
        Object object2 = map1.get("1");
        assertEquals(object1, object2);
    }
    
    @Test
    public void testNotSynchornizedMap() {
        Map<String, HashMap<String, String>> map1 = new DefaultConcurrentHashMap<>(new HashMap<String, String>(), false);
        Map<String, String> object1 = map1.get("1");
        assertNotNull(object1);
        assertFalse(object1.getClass().getName().equals("java.util.Collections$SynchronizedMap"));

        object1.put("a", "b");
        Object object2 = map1.get("1");
        assertEquals(object1, object2);
        Object object3 = map1.get("2");
        assertNotEquals(object1,object3);
    }

    @Test
    public void testSynchornizedCollection() {
        // set
        Map<String, HashSet<String>> map1 = new DefaultConcurrentHashMap<>(new HashSet<String>(), true);
        Set<String> set1 = map1.get("1");
        assertNotNull(set1);
        assertEquals("java.util.Collections$SynchronizedSet", set1.getClass().getName());
        set1.add("a");

        Object set2 = map1.get("1");
        assertEquals(set1, set2);

        // list
        Map<String, ArrayList<String>> listMap1 = new DefaultConcurrentHashMap<>(new ArrayList<String>(), true);
        List<String> list1 = listMap1.get("1");
        assertNotNull(list1);
        assertEquals("java.util.Collections$SynchronizedRandomAccessList", list1.getClass().getName());
        list1.add("a");

        Object list2 = listMap1.get("1");
        assertEquals(list1, list2);

    }
    
    @Test
    public void testNotSynchornizedCollection() {
        // set
        Map<String, HashSet<String>> map1 = new DefaultConcurrentHashMap<>(new HashSet<String>());
        Set<String> set1 = map1.get("1");
        assertFalse(set1.getClass().getName().equals("java.util.Collections$SynchronizedSet"));
        assertNotNull(set1);
        assertTrue(set1 instanceof Set);
        set1.add("a");

        Object set2 = map1.get("1");
        assertEquals(set1, set2);

        // list
        Map<String, ArrayList<String>> listMap1 = new DefaultConcurrentHashMap<>(new ArrayList<String>());
        List<String> list1 = listMap1.get("1");
        assertNotNull(list1);
        assertFalse(list1.getClass().getName().equals("java.util.Collections$SynchronizedRandomAccessList"));
        list1.add("a");

        Object list2 = listMap1.get("1");
        assertEquals(list1, list2);

    }

}
