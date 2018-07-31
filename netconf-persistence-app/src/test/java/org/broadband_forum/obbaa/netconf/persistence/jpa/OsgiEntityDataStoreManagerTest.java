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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Metamodel;

import org.broadband_forum.obbaa.netconf.persistence.PagingInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Created by mn on 10/4/16.
 */
public class OsgiEntityDataStoreManagerTest {
    OsgiEntityDataStoreManager m_osgiEntityDataStoreManager = null;

    @Mock
    private EntityManager m_entityManager;

    @Mock
    private CriteriaBuilder m_criteriaBuilder;

    @Mock
    private CriteriaQuery m_criteriaQuery;

    @Mock
    private CriteriaQuery all;

    @Mock
    private Root rootEntry;

    @Mock
    TypedQuery m_allQuery;

    String m_dummy = "test";
    String orderByColumn = "condition";
    String field = "field";
    String colname = "colname";
    String colvalue = "colvalue";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        m_osgiEntityDataStoreManager = new OsgiEntityDataStoreManager();
        m_osgiEntityDataStoreManager.setEntityManager(m_entityManager);
        when(m_entityManager.getCriteriaBuilder()).thenReturn(m_criteriaBuilder);
        when(m_criteriaBuilder.createQuery(String.class)).thenReturn(m_criteriaQuery);
        when(m_criteriaBuilder.createQuery(Long.class)).thenReturn(m_criteriaQuery);
        when(m_criteriaQuery.from(String.class)).thenReturn(rootEntry);
        when(m_criteriaQuery.select(rootEntry)).thenReturn(all);
        when(m_entityManager.createQuery(all)).thenReturn(m_allQuery);
    }

    @Test
    public void testCreate() {
        m_osgiEntityDataStoreManager.create(m_dummy);
        verify(m_entityManager).persist(m_dummy);
        assertTrue(m_osgiEntityDataStoreManager.create(m_dummy));
    }

    @Test
    public void testFindById() {
        int primaryKey = 1;
        when(m_osgiEntityDataStoreManager.getEntityManager().find(String.class, primaryKey,
                LockModeType.PESSIMISTIC_READ)).thenReturn("TEST");
        assertEquals("TEST", m_osgiEntityDataStoreManager.findById(String.class, primaryKey));
    }

    @Test
    public void testFindByUniqueKeys() {
        Map<String, Object> keys = new HashMap<>();
        keys.put("key1", "value1");
        List test = new ArrayList<>();
        when(m_allQuery.getSingleResult()).thenReturn(test);
        List<Predicate> predicates = new ArrayList<>();
        assertEquals(test, m_osgiEntityDataStoreManager.findByUniqueKeys(String.class, keys));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindAll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findAll(String.class));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindAllWithOrderByColumn() {
        List<String> test = new ArrayList<>();
        test.add(0, "condition");
        test.add(1, "not");
        test.add(2, "condition");
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findAll(String.class, orderByColumn));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindRange() {
        Double first = 1D;
        Double second = 2D;
        List<String> test = new ArrayList<>();
        test.add(0, "condition");
        test.add(1, "not");
        test.add(2, "condition");
        when(m_allQuery.getResultList()).thenReturn(test);
        PredicateCondition condition = PredicateCondition.EQUAL;
        condition.addCondition(m_criteriaBuilder, m_criteriaQuery, rootEntry, field, first, second);
        assertEquals(test, m_osgiEntityDataStoreManager.findRange(String.class, orderByColumn, condition, first));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchValuewithOrderBycolumnNUll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, Object> matchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        List<Predicate> predicates = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchValue(String.class, matchValues));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchValueWithNotmatchvaluesNUll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        List<Predicate> predicates = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchValue(String.class, matchValues, orderByColumn));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(all).orderBy(Arrays.asList(m_criteriaBuilder.asc(rootEntry.get(orderByColumn))));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchAndNotMatchValueOrderNUll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        List<Predicate> predicates = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchAndNotMatchValue(String.class, matchValues,
                notMatchValues, null));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(all, never()).orderBy(m_criteriaBuilder.asc(rootEntry.get(orderByColumn)));
        verify(all, never()).orderBy(m_criteriaBuilder.desc(rootEntry.get(orderByColumn)));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchAndNotMatchValueisDescFalseNUll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        List<Predicate> predicates = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchAndNotMatchValue(String.class, matchValues,
                notMatchValues, orderByColumn, null));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(all).orderBy(Arrays.asList(m_criteriaBuilder.asc(rootEntry.get(orderByColumn))));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchAndNotMatchValueisDescTrueNUll() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        List<Predicate> predicates = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchAndNotMatchValue(String.class, matchValues,
                notMatchValues, orderByColumn, true, null));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(all).orderBy(Arrays.asList(m_criteriaBuilder.desc(rootEntry.get(orderByColumn))));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByLike() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByLike(String.class, colname, colvalue, 1));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
        verify(m_allQuery).setMaxResults(1);
    }

    @Test
    public void testFindByIsNotNull() {
        List test = new ArrayList<>();
        List attributes = new ArrayList();
        attributes.add(0, "attrib1");
        attributes.add(1, "attrib2");
        attributes.add(2, "attrib3");
        when(m_allQuery.getResultList()).thenReturn(test);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByIsNotNull(String.class, attributes));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchAndNotMatchValueswithorderByColumnNull() {
        List test = new ArrayList<>();
        Path<Object> pathObj = mock(Path.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, List<Object>> matchValues = new HashMap<>();
        Map<String, List<Object>> notMatchValues = new HashMap<>();
        List<Object> match1List = new ArrayList<>();
        List<Object> match2List = new ArrayList<>();
        matchValues.put("match1", match1List);
        notMatchValues.put("nomatch1", match2List);
        when(rootEntry.get("match1")).thenReturn(pathObj);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test,
                m_osgiEntityDataStoreManager.findByMatchAndNotMatchValues(String.class, matchValues, notMatchValues));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(all, never()).orderBy(m_criteriaBuilder.asc(rootEntry.get(orderByColumn)));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testFindByMatchAndNotMatchValues2() {
        List test = new ArrayList<>();
        Path<Object> pathObj = mock(Path.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        Map<String, List<Object>> matchValues = new HashMap<>();
        Map<String, List<Object>> notMatchValues = new HashMap<>();
        List<Object> match1List = new ArrayList<>();
        List<Object> match2List = new ArrayList<>();
        matchValues.put("match1", match1List);
        notMatchValues.put("nomatch1", match2List);
        when(rootEntry.get("match1")).thenReturn(pathObj);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findByMatchAndNotMatchValues(String.class, matchValues,
                notMatchValues, orderByColumn));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(all).orderBy(Arrays.asList(m_criteriaBuilder.asc(rootEntry.get(orderByColumn))));
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testCountByMatchAndNotMatchValue() {
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        Expression<Long> count = mock(Expression.class);
        TypedQuery<Long> query = mock(TypedQuery.class);
        Path<Object> pathObj = mock(Path.class);
        Long test = 1l;
        when(m_entityManager.createQuery(m_criteriaQuery)).thenReturn(query);
        when(rootEntry.get("match1")).thenReturn(pathObj);
        when(m_criteriaBuilder.count(rootEntry)).thenReturn(count);
        when(query.getSingleResult()).thenReturn(test);
        Long result = m_osgiEntityDataStoreManager.countByMatchAndNotMatchValue(String.class, matchValues,
                notMatchValues);
        assertEquals(test, result);
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(Long.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(count);
        verify(m_entityManager).createQuery(m_criteriaQuery);
    }

    @Test
    public void testCountByMatchAndNotMatchValueWithLockMode() {
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        Expression<Long> count = mock(Expression.class);
        TypedQuery<Long> query = mock(TypedQuery.class);
        Path<Object> pathObj = mock(Path.class);
        Long test = 1l;
        when(m_entityManager.createQuery(m_criteriaQuery)).thenReturn(query);
        when(rootEntry.get("match1")).thenReturn(pathObj);
        when(m_criteriaBuilder.count(rootEntry)).thenReturn(count);
        when(query.getSingleResult()).thenReturn(test);
        Long result = m_osgiEntityDataStoreManager.countByMatchAndNotMatchValue(String.class, matchValues,
                notMatchValues, LockModeType.NONE);
        assertEquals(test, result);
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(Long.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(count);
        verify(m_entityManager).createQuery(m_criteriaQuery);
        verify(query).setLockMode(LockModeType.NONE);
    }

    @Test
    public void testDelete() {
        m_osgiEntityDataStoreManager.delete(m_dummy);
        verify(m_entityManager).remove(m_dummy);
        List entities = new ArrayList();

        Object entity1 = mock(Object.class);
        Object entity2 = mock(Object.class);
        entities.add(entity1);
        entities.add(entity2);
        when(m_allQuery.getResultList()).thenReturn(entities);
        m_osgiEntityDataStoreManager.delete(m_dummy.getClass(), Collections.emptyMap());

        verify(m_entityManager).remove(entity1);
        verify(m_entityManager).remove(entity2);

        m_osgiEntityDataStoreManager.delete(m_dummy, Collections.emptyMap());

        verify(m_entityManager, times(2)).remove(entity1);
        verify(m_entityManager, times(2)).remove(entity2);
    }

    @Test
    public void testDeleteById() {
        Object entity1 = mock(Object.class);
        when(m_entityManager.find(Object.class, new Long(1), LockModeType.PESSIMISTIC_READ)).thenReturn(null);
        m_osgiEntityDataStoreManager.deleteById(Object.class, new Long(1));
        verify(m_entityManager, never()).remove(entity1);

        when(m_entityManager.find(Object.class, new Long(1), LockModeType.PESSIMISTIC_READ)).thenReturn(entity1);
        m_osgiEntityDataStoreManager.deleteById(Object.class, new Long(1));
        verify(m_entityManager).remove(entity1);
    }

    @Test
    public void testFlush() {
        m_osgiEntityDataStoreManager.flush();
        verify(m_entityManager).flush();
    }

    @Test
    public void testBuildQueryPath() {
        String result = m_osgiEntityDataStoreManager.buildQueryPath("a", "b", "c");
        assertEquals("a.b.c", result);
    }

    @Test
    public void testFindWithPaging() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        PagingInput pagingInput = mock(PagingInput.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPaging(String.class, pagingInput));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_allQuery).setFirstResult(pagingInput.getFirstResult());
        verify(m_allQuery).setMaxResults(pagingInput.getMaxResult());
        verify(m_entityManager).createQuery(all);
        verify(m_allQuery).getResultList();
    }

    @Test
    public void testFindWithPagingNULL() {
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        PagingInput pagingInput = mock(PagingInput.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPaging(String.class, null));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_allQuery, never()).setFirstResult(pagingInput.getFirstResult());
        verify(m_allQuery, never()).setMaxResults(pagingInput.getMaxResult());
        verify(m_entityManager).createQuery(all);
        verify(m_allQuery).getResultList();
    }

    @Test
    public void testFindWithPagingwithMatch() {
        List test = new ArrayList<>();
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        when(m_allQuery.getResultList()).thenReturn(test);
        PagingInput pagingInput = mock(PagingInput.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        List<Predicate> predicates = new ArrayList<>();
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPaging(String.class, pagingInput, matchValues));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_allQuery).setFirstResult(pagingInput.getFirstResult());
        verify(m_allQuery).setMaxResults(pagingInput.getMaxResult());
        verify(m_entityManager).createQuery(all);
        verify(m_allQuery).getResultList();
    }

    @Test
    public void testFindWithPagingwithMatchPageNull() {
        List test = new ArrayList<>();
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        when(m_allQuery.getResultList()).thenReturn(test);
        PagingInput pagingInput = mock(PagingInput.class);
        when(m_allQuery.getResultList()).thenReturn(test);
        List<Predicate> predicates = new ArrayList<>();
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPaging(String.class, null, matchValues));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).where(predicates.toArray(new Predicate[predicates.size()]));
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_allQuery, never()).setFirstResult(pagingInput.getFirstResult());
        verify(m_allQuery, never()).setMaxResults(pagingInput.getMaxResult());
        verify(m_entityManager).createQuery(all);
        verify(m_allQuery).getResultList();
    }

    @Test
    public void testFindWithPagingwithColumns() {
        List test = new ArrayList<>();
        List<String> col = new ArrayList<>();
        col.add(0, "condition");
        col.add(1, "not");
        col.add(2, "condition");
        Map<String, Object> matchValues = new HashMap<>();
        Map<String, Object> notMatchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        notMatchValues.put("nomatch1", "value1");
        PagingInput pagingInput = new PagingInput(1, 10);
        CriteriaQuery<String> criteriaQuery = mock(CriteriaQuery.class);
        Root<String> rootEntry = mock(Root.class);
        CriteriaQuery<String> all1 = mock(CriteriaQuery.class);
        TypedQuery<String> allQuery1 = mock(TypedQuery.class);
        List<Selection<?>> selections = new ArrayList<Selection<?>>();
        selections.add(rootEntry.get(col.get(0)));
        selections.add(rootEntry.get(col.get(1)));
        selections.add(rootEntry.get(col.get(2)));
        when(m_entityManager.getCriteriaBuilder()).thenReturn(m_criteriaBuilder);
        when(m_criteriaBuilder.createQuery(String.class)).thenReturn(criteriaQuery);
        when(criteriaQuery.from(String.class)).thenReturn(rootEntry);
        when(criteriaQuery.multiselect(selections)).thenReturn(all1);
        when(m_entityManager.createQuery(all1)).thenReturn(allQuery1);
        when(allQuery1.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPaging(String.class, pagingInput, col));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(criteriaQuery).from(String.class);
        verify(allQuery1).setFirstResult(pagingInput.getFirstResult());
        verify(allQuery1).setMaxResults(pagingInput.getMaxResult());
        verify(allQuery1).getResultList();
    }

    @Test
    public void testIsOpen() {
        m_osgiEntityDataStoreManager.isOpen();
        verify(m_entityManager).isOpen();

    }

    @Test
    public void testGetMetaModel() {
        Metamodel meta = mock(Metamodel.class);
        when(m_entityManager.getMetamodel()).thenReturn(meta);
        assertEquals(meta, m_osgiEntityDataStoreManager.getMetaModel());
        verify(m_entityManager).getMetamodel();
    }

    @Test
    public void testDeletewithEntityClass() {
        Map val = mock(Map.class);
        List lists = new ArrayList<String>();
        when(m_entityManager.createQuery(any(CriteriaQuery.class))).thenReturn(m_allQuery);
        when(m_allQuery.getResultList()).thenReturn(lists);
        assertTrue(m_osgiEntityDataStoreManager.delete(String.class, val));

    }


    @Test
    public void testDeleteAll() {
        int removedNum = 1;
        CriteriaDelete cd = mock(CriteriaDelete.class);
        javax.persistence.Query query = mock(javax.persistence.Query.class);
        when(m_criteriaBuilder.createCriteriaDelete(String.class)).thenReturn(cd);
        when(m_entityManager.createQuery(cd)).thenReturn(query);
        when(query.executeUpdate()).thenReturn(removedNum);
        assertEquals(removedNum, m_osgiEntityDataStoreManager.deleteAll(String.class));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createCriteriaDelete(String.class);
        verify(cd).from(String.class);
        verify(m_entityManager).createQuery(cd);
        verify(query).executeUpdate();
    }

    @Test
    public void testFindLike() {
        Map<String, String> matchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findLike(String.class, matchValues));
        verify(m_entityManager).getCriteriaBuilder();
        verify(m_criteriaBuilder).createQuery(String.class);
        verify(m_criteriaQuery).from(String.class);
        verify(m_criteriaQuery).select(rootEntry);
        verify(m_entityManager).createQuery(all);
    }

    @Test
    public void testDeleteLike() {
        Map val = mock(Map.class);
        List lists = new ArrayList<>();
        when(m_osgiEntityDataStoreManager.findLike(String.class, val)).thenReturn(lists);
        assertTrue(m_osgiEntityDataStoreManager.deleteLike(String.class, val));

    }

    @Test
    public void testFindWithPagingAndOrderByColumn() {
        Map<String, Object> matchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        PagingInput pagingInput = mock(PagingInput.class);
        List test = new ArrayList<>();
        when(m_allQuery.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findWithPagingAndOrderByColumn(String.class, pagingInput,
                matchValues, orderByColumn, true));
    }

    @Test
    public void testFindSelectedColumnsWithMatchedValues() {
        Map<String, Object> matchValues = new HashMap<>();
        matchValues.put("match1", "value1");
        PagingInput pagingInput = new PagingInput(1, 10);
        Path<Object> pathObj = mock(Path.class);
        List test = new ArrayList<>();
        Predicate predicate = mock(Predicate.class);
        List<String> selectedAttr = new ArrayList<>();
        selectedAttr.add(0, "attr1");
        selectedAttr.add(1, "attr2");
        CriteriaQuery<String> all1 = mock(CriteriaQuery.class);
        TypedQuery<String> allQuery1 = mock(TypedQuery.class);
        List<Selection<?>> selections = new ArrayList<Selection<?>>();
        selections.add(rootEntry.get(selectedAttr.get(0)));
        selections.add(rootEntry.get(selectedAttr.get(1)));
        when(rootEntry.get("match1")).thenReturn(pathObj);
        when(m_criteriaQuery.multiselect(selections)).thenReturn(all1);
        when(m_entityManager.createQuery(all1)).thenReturn(allQuery1);
        when(m_criteriaBuilder.equal(pathObj, matchValues.get("match1"))).thenReturn(predicate);
        when(allQuery1.getResultList()).thenReturn(test);
        assertEquals(test, m_osgiEntityDataStoreManager.findSelectedColumnsWithMatchedValues(String.class, pagingInput,
                matchValues, selectedAttr));
    }

    @Test
    public void testMerge() {
        Object entity = mock(Object.class);
        m_osgiEntityDataStoreManager.merge(entity);
        verify(m_entityManager).merge(entity);
    }
}
