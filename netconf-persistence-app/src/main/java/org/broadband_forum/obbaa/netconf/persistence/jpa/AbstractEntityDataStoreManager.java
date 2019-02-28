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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.messages.LogUtil;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;

/**
 * Created by keshava on 2/19/16.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractEntityDataStoreManager implements EntityDataStoreManager {
    private static final Logger LOGGER = Logger.getLogger(AbstractEntityDataStoreManager.class);

    private static final String DOT = ".";
    private static final String DOT_PATTERN = "\\.";


    private <E> List<Predicate> getPredicates(List<Predicate> predicates, Map<String, Object> values, CriteriaBuilder criteriaBuilder,
                                              Root rootEntry, boolean matchValue) {

        if (values != null) {
            for (String attributeName : values.keySet()) {
                Path<Object> pathObject = getPathObject(rootEntry, attributeName);
                if (pathObject != null) {
                    Predicate predicate = null;
                    if (matchValue) {
                        predicate = criteriaBuilder.equal(pathObject, values.get(attributeName));
                    } else {
                        predicate = criteriaBuilder.notEqual(pathObject, values.get(attributeName));
                    }
                    predicates.add(predicate);
                }
            }
        }
        return predicates;
    }

    private <E> Predicate getLikePredicate(String columnName, String value, CriteriaBuilder criteriaBuilder, Root rootEntry) {

        StringBuilder stringBuilder = new StringBuilder(value);
        stringBuilder.append("%");
        Predicate predicate = criteriaBuilder.like(rootEntry.get(columnName), stringBuilder.toString());
        return predicate;
    }

    private <E> Predicate getLikePredicateWithFieldsContain(String columnName, String value, CriteriaBuilder criteriaBuilder, Root rootEntry) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%");
        stringBuilder.append(value.toLowerCase());
        stringBuilder.append("%");

        return criteriaBuilder.like(criteriaBuilder.lower(rootEntry.get(columnName)), stringBuilder.toString());
    }

    private void addOrderByAsc(CriteriaQuery<?> all, CriteriaBuilder criteriaBuilder, Root<?> rootEntry, String orderByColumn) {
        addOrderByAsc(all, criteriaBuilder, rootEntry, Arrays.asList(orderByColumn));
    }

    private void addOrderByAsc(CriteriaQuery<?> all, CriteriaBuilder criteriaBuilder, Root<?> rootEntry, List<String> orderByColumns) {
        List<Order> orderList = new ArrayList<Order>();

        for (String orderByColumn:orderByColumns) {
            orderList.add(criteriaBuilder.asc(rootEntry.get(orderByColumn)));
        }

        if (!orderList.isEmpty()) {
            all.orderBy(orderList);
        }
    }

    private void addOrderByDesc(CriteriaQuery<?> all, CriteriaBuilder criteriaBuilder, Root<?> rootEntry, String orderByColumn) {
        addOrderByDesc(all, criteriaBuilder, rootEntry, Arrays.asList(orderByColumn));
    }

    private void addOrderByDesc(CriteriaQuery<?> all, CriteriaBuilder criteriaBuilder, Root<?> rootEntry, List <String> orderByColumns) {
        List<Order> orderList = new ArrayList<Order>();

        for (String orderByColumn:orderByColumns) {
            orderList.add(criteriaBuilder.desc(rootEntry.get(orderByColumn)));
        }

        if (!orderList.isEmpty()) {
            all.orderBy(orderList);
        }
    }


    @Override
    public <E> boolean create(E entity) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("create entity: " + entity);
        }
        getEntityManager().persist(entity);
        return true;
    }

    @Override
    public <E> E findById(Class<E> entityClass, Object primaryKey) {
    	return findById(entityClass, primaryKey, LockModeType.PESSIMISTIC_READ);
    }

    @Override
    public <E> E findById(Class<E> entityClass, Object primaryKey, LockModeType lockMode) {
        LogUtil.logDebug(LOGGER, "findById called for class: %s with pk: %s, LockMode:%s ", entityClass, primaryKey, lockMode);
        E entity = getEntityManager().find(entityClass, primaryKey, lockMode);
        LogUtil.logDebug(LOGGER, "findById returned for class: %s with pk: %s ", entityClass, primaryKey, lockMode);
        return entity;
    }

    public <E> E findByUniqueKeys(Class<E> entityClass, Map<String, Object> keys) {
        List<E> values = findByMatchValue(entityClass,keys);
        if(values == null || values.isEmpty()){
            return null;
        }
        return values.get(0);
    }

    @Override
    public <E> List<E> findAll(Class<E> pojoClass) {
        return findAll(pojoClass, (String)null);
    }

    @Override
    public <E> List<E> findAll(Class<E> pojoClass, String orderByColumn) {
        List<String> orderByColumns = null;
        if (orderByColumn == null) {
            orderByColumns = Collections.emptyList();
        } else {
            orderByColumns = Arrays.asList(orderByColumn);
        }

        return findAll(pojoClass, orderByColumns);
    }

    @Override
    public <E> List<E> findAll(Class<E> pojoClass, List<String> orderByColumn) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(pojoClass);
        Root<E> rootEntry = cq.from(pojoClass);
        CriteriaQuery<E> all = cq.select(rootEntry);
        addOrderByAsc(all, cb, rootEntry, orderByColumn);
        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        return logAndReturnQueryResult("findAll", pojoClass, allQuery);
    }

    @Override
    public <E> List<E> findRange(Class<E> pojoClass, String predicateColumn, PredicateCondition condition, Double value) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(pojoClass);
        Root<E> rootEntry = cq.from(pojoClass);
        CriteriaQuery<E> all = cq.select(rootEntry);
        condition.addCondition(cb, all, rootEntry, predicateColumn, value, null);
        TypedQuery<E> allQuery = getEntityManager().createQuery(all);

        return logAndReturnQueryResult("findRange", pojoClass, allQuery);
    }

    private TypedQuery m_forTest;
    
    TypedQuery getTypedQuery(){
    	return m_forTest;
    }
    private <E> E logAndReturnQuerySingleResult(String methodName, Class<E> entityClass, TypedQuery<E> allQuery) {
    	
    	LockModeType lockModeType = allQuery.getLockMode();
    	
    	if (lockModeType == null || !lockModeType.equals(LockModeType.PESSIMISTIC_WRITE)){
    		allQuery.setLockMode(LockModeType.PESSIMISTIC_READ);
    	}
    	m_forTest = allQuery;
        LogUtil.logDebug(LOGGER, "%s called for class: %s and query: %s", methodName, entityClass, allQuery);
        E resultList = allQuery.getSingleResult();
        LogUtil.logDebug(LOGGER, "%s returned for class: %s and query: %s", methodName, entityClass, allQuery);
        return resultList;
    }

    @Override
    public <E> List<E> findByMatchValue(Class<E> pojoClass, Map<String, Object> matchValues) {
        return findByMatchValue(pojoClass, matchValues, (String) null);
    }

    @Override
    public <E> List<E> findByMatchValue(Class<E> pojoClass, Map<String, Object> matchValues, String orderByColumn) {
        return findByMatchAndNotMatchValue(pojoClass, matchValues, null, orderByColumn, null);
    }

    public <E> List<E> findByMatchValue(Class<E> pojoClass, Map<String, Object> matchValues, LockModeType lockModeType) {
        return findByMatchAndNotMatchValue(pojoClass, matchValues, null, lockModeType);
    }

    @Override
    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues, Map<String, Object>
            notMatchValues, LockModeType lockModeType) {
        return findByMatchAndNotMatchValue(entityClass, matchValues, notMatchValues, null, lockModeType);
    }

    @Override
    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues,
                                                   Map<String, Object> notMatchValues, String orderByColumn, LockModeType lockModeType) {
        return findByMatchAndNotMatchValue(entityClass, matchValues, notMatchValues, orderByColumn, false, lockModeType);
    }

    @Override
    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues,
                                                   Map<String, Object> notMatchValues, String orderByColumn,
                                                   boolean isDesc, LockModeType lockModeType) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        getPredicates(predicates, matchValues, criteriaBuilder, rootEntry, true);
        getPredicates(predicates, notMatchValues, criteriaBuilder, rootEntry, false);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);
        if (orderByColumn != null) {
            if (isDesc) {
                addOrderByDesc(all, criteriaBuilder, rootEntry, orderByColumn);
            } else {
                addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);
            }
        }

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if (lockModeType != null) {
            allQuery.setLockMode(lockModeType);
        }
        return logAndReturnQueryResult("findByMatchAndNotMatchValue", entityClass, allQuery);

    }

    /**
     * @param entityClass
     * @param columnName
     * @param value       : Starting with given string value
     * @return
     */
    @Override
    public <E> List<E> findByLike(Class<E> entityClass, String columnName, String value) {
    	return findByLike(entityClass, columnName, value, -1);
    }

    /**
     * @param entityClass
     * @param columnName
     * @param value       : Starting with given string value
     * @param count       : Return top results
     * @return
     */
    @Override
    public <E> List<E> findByLike(Class<E> entityClass, String columnName, String value, int count) {
        Map<String, String> columnValueMap = new HashMap<>();
        columnValueMap.put(columnName, value);
        return findByLike(entityClass, columnValueMap, null, count);
    }

    @Override
    public <E> List<E> findByLike(Class klass, Map<String, String> columnKeyMap, Map<String, Object> matchValues, int maxResults){
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(klass);
        Root<E> rootEntry = criteriaQuery.from(klass);

        List<Predicate> allPredicates = getLikePredicates(columnKeyMap, criteriaBuilder, rootEntry);
        TypedQuery<E> allQuery = getTypedQueryWithMaxResult(klass, allPredicates, matchValues, criteriaBuilder, criteriaQuery, rootEntry, maxResults);

        return logAndReturnQueryResult("findByLike", klass, allQuery);
    }

    @Override
    public <E> List<E> findByLikeWithFieldsContain(Class klass, Map<String, String> columnKeyMap, Map<String, Object> matchValues, int maxResults){
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(klass);
        Root<E> rootEntry = criteriaQuery.from(klass);

        List<Predicate> allPredicates = getLikePredicatesWithFieldsContain(columnKeyMap, criteriaBuilder, rootEntry);
        TypedQuery<E> allQuery = getTypedQueryWithMaxResult(klass, allPredicates, matchValues, criteriaBuilder, criteriaQuery, rootEntry, maxResults);

        return logAndReturnQueryResult("findByLikeWithFieldsContain", klass, allQuery);
    }

    private <E> TypedQuery getTypedQueryWithMaxResult(Class klass, List<Predicate> predicates, Map<String, Object> matchValues, CriteriaBuilder criteriaBuilder, CriteriaQuery criteriaQuery,
                                         Root rootEntry, int maxResults) {
        getPredicates(predicates, matchValues, criteriaBuilder, rootEntry, true);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        CriteriaQuery all = criteriaQuery.select(rootEntry);

        String orderByColumn = getIdAttribute(klass);
        addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if(maxResults >= 0) {
            allQuery.setMaxResults(maxResults);
        }

        return allQuery;
    }

    private <E> List<Predicate> getLikePredicates(Map<String, String> columnKeyMap, CriteriaBuilder criteriaBuilder, Root<E> rootEntry) {
        List<Predicate> predicates = new ArrayList<>();
        for(Entry<String, String> entry : columnKeyMap.entrySet()){
            predicates.add(getLikePredicate(entry.getKey(), entry.getValue(), criteriaBuilder, rootEntry));
        }
        return predicates;
    }

    private <E> List<Predicate> getLikePredicatesWithFieldsContain(Map<String, String> columnKeyMap, CriteriaBuilder criteriaBuilder, Root<E> rootEntry) {
        List<Predicate> predicates = new ArrayList<>();
        for(Entry<String, String> entry : columnKeyMap.entrySet()){
            predicates.add(getLikePredicateWithFieldsContain(entry.getKey(), entry.getValue(), criteriaBuilder, rootEntry));
        }
        return predicates;
    }

    @Override
    public <E> List<E> findByIsNotNull(Class<E> entityClass, List<String> attributes) {
        EntityManager entityManager = getEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        for (String attribute : attributes) {
            predicates.add(criteriaBuilder.isNotNull(rootEntry.get(attribute)));
        }

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);

        TypedQuery<E> allQuery = entityManager.createQuery(all);
        return logAndReturnQueryResult("findByIsNotNull", entityClass, allQuery);
    }

    private <E> List<E> logAndReturnQueryResult(final String methodName, Class<E> entityClass, TypedQuery<E> allQuery) {
        LogUtil.logDebug(LOGGER, "%s called for class: %s and query: %s", methodName, entityClass, allQuery);
        
    	LockModeType lockModeType = allQuery.getLockMode();
    	if (lockModeType == null){
    		allQuery.setLockMode(LockModeType.PESSIMISTIC_READ);
    	}
    	
        List<E> resultList = allQuery.getResultList();
        LogUtil.logDebug(LOGGER, "%s returned for class: %s and query: %s", methodName, entityClass, allQuery);
        return resultList;
    }

    @Override
    public <E> List<E> findByMatchAndNotMatchValues(Class<E> entityClass, Map<String, List<Object>> matchValues, Map<String,
            List<Object>> notMatchValues) {
        return findByMatchAndNotMatchValues(entityClass, matchValues, notMatchValues, null);
    }

    @Override
    public <E> List<E> findByMatchAndNotMatchValues(Class<E> entityClass, Map<String, List<Object>> matchValues, Map<String,
            List<Object>> notMatchValues, String orderByColumn) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> cq = cb.createQuery(entityClass);
        Root<E> rootEntry = cq.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();

        // match values, build predicates
        if (matchValues != null) {
            for (Map.Entry<String, List<Object>> entry : matchValues.entrySet()) {
                String attributeName = entry.getKey();
                Path<Object> pathObject = getPathObject(rootEntry, attributeName);
                List<Predicate> matchPredicates = new ArrayList<>();
                for (Object matchValue : entry.getValue()) {
                    Predicate predicate = cb.equal(pathObject, matchValue);
                    matchPredicates.add(predicate);
                }
                Predicate orPredicate = cb.or(matchPredicates.toArray(new Predicate[matchPredicates.size()]));
                predicates.add(orPredicate);
            }
        }

        // not match values, build predicates
        if (notMatchValues != null) {
            for (Map.Entry<String, List<Object>> entry : notMatchValues.entrySet()) {
                String attributeName = entry.getKey();
                Path<Object> pathObject = getPathObject(rootEntry, attributeName);
                for (Object nonMatchValue : entry.getValue()) {
                    Predicate predicate = cb.notEqual(pathObject, nonMatchValue);
                    predicates.add(predicate);
                }
            }
        }

        cq.where(predicates.toArray(new Predicate[predicates.size()]));
        CriteriaQuery<E> all = cq.select(rootEntry);
        if (orderByColumn != null) {
            addOrderByAsc(all, cb, rootEntry, orderByColumn);
        }

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        return logAndReturnQueryResult("findByMatchAndNotMatchValues", entityClass, allQuery);
    }

    @Override
    public Long countByMatchAndNotMatchValue(Class entityClass, Map<String, Object> matchValues, Map<String, Object> notMatchValues) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root rootEntry = criteriaQuery.from(entityClass);
        List<Predicate> predicates = new ArrayList<>();
        getPredicates(predicates, matchValues, criteriaBuilder, rootEntry, true);
        getPredicates(predicates, notMatchValues, criteriaBuilder, rootEntry, false);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        Expression<Long> count = criteriaBuilder.count(rootEntry);
        criteriaQuery.select(count);
        TypedQuery<Long> query = getEntityManager().createQuery(criteriaQuery);
        return logAndReturnQuerySingleResult("countByMatchAndNotMatchValue", entityClass, query);
    }
    
    @Override
    public Long countByMatchAndLikeValue(Class entityClass, Map<String, String> matchValues, Map<String, String> likeValues, LockModeType lockMode) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root rootEntry = criteriaQuery.from(entityClass);
        
        List<Predicate> andPredicates = new ArrayList<>();
        if (matchValues != null) {
            for (Entry<String, String> entry : matchValues.entrySet()) {
                Path<Object> pathObject = getPathObject(rootEntry, entry.getKey());
                if (pathObject == null){
                    pathObject = rootEntry.get(entry.getKey());
                }
                Predicate predicate = criteriaBuilder.equal(pathObject, ConvertUtils.convert(entry.getValue(), pathObject.getJavaType()));
                andPredicates.add(predicate);
            }
        }

        List<Predicate> orPredicates = new ArrayList<>();
        if (likeValues != null) {
            for (Entry<String, String> entry : likeValues.entrySet()) {
                Path<Object> pathObject = getPathObject(rootEntry, entry.getKey());
                if (pathObject == null){
                    pathObject = rootEntry.get(entry.getKey());
                }
                Predicate predicate = getOrPredicate(entry.getValue(), criteriaBuilder, pathObject);
                orPredicates.add(predicate);
            }
        }
        
        if (!orPredicates.isEmpty()) {
            Predicate orPredicate  = criteriaBuilder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
            andPredicates.add(orPredicate);
        }
        
        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));
        Expression<Long> count = criteriaBuilder.count(rootEntry);
        criteriaQuery.select(count);
        TypedQuery<Long> query = getEntityManager().createQuery(criteriaQuery);
        if(lockMode != null) {
            query.setLockMode(lockMode);
        }
        return logAndReturnQuerySingleResult("countByMatchAndLikeValue", entityClass, query);
    }

    @Override
    public Long countByMatchAndNotMatchValue(Class entityClass, Map<String, Object> matchValues, Map<String, Object> notMatchValues,
                                             LockModeType lockMode) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root rootEntry = criteriaQuery.from(entityClass);
        List<Predicate> predicates = new ArrayList<>();
        getPredicates(predicates, matchValues, criteriaBuilder, rootEntry, true);
        getPredicates(predicates, notMatchValues, criteriaBuilder, rootEntry, false);
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        Expression<Long> count = criteriaBuilder.count(rootEntry);
        criteriaQuery.select(count);
        TypedQuery<Long> query  = getEntityManager().createQuery(criteriaQuery);
        if(lockMode != null) {
            query.setLockMode(lockMode);
        }
        LogUtil.logDebug(LOGGER, "%s called for class: %s and query: %s with lock-mode: %s", "countByMatchAndNotMatchValue",
                entityClass, query, lockMode);
        Long resultList = query.getSingleResult();
        LogUtil.logDebug(LOGGER, "%s returned for class: %s and query: %s with lock-mode: %s", "countByMatchAndNotMatchValue",
                entityClass, query, lockMode);
        return resultList;
    }

    @Override
    public <E> boolean merge(E entity) {
        LogUtil.logDebug(LOGGER, "Update entity called for : %s", entity);
        getEntityManager().merge(entity);
        LogUtil.logDebug(LOGGER, "Update entity returned for : %s", entity);
        return true;
    }

    @Override
    public void persist(Object entity) {
        LogUtil.logDebug(LOGGER, "persist entity called for : %s", entity);
        getEntityManager().persist(entity);
        LogUtil.logDebug(LOGGER, "persist entity returned for : %s", entity);
    }

    @Override
    public <E> boolean delete(E entity) {
        LogUtil.logDebug(LOGGER, "delete entity called for : %s", entity);
        EntityManager em = getEntityManager();
        em.remove(entity);
        LogUtil.logDebug(LOGGER, "delete entity returned for : %s", entity);
        return true;
    }

    @Override
    public void flush() {
        LogUtil.logDebug(LOGGER, "flush called");
        getEntityManager().flush();
        LogUtil.logDebug(LOGGER, "flush returned");
    }

    public static synchronized EntityDataStoreManager getPersistenceManager(JPAEntityManagerFactory entityManagerFactory) {
        return new JPAEntityDataStoreManager();
    }

    /**
     * @param parentAttribute
     * @param childAttributes
     * @return a queryPath
     * Eg: parentAttribute = "a", childAttributes = [b, c], returned result = "a.b.c"
     */
    public static String buildQueryPath(String parentAttribute, String... childAttributes) {
        StringBuilder result = new StringBuilder(parentAttribute);
        for (String childAttribute : childAttributes) {
            result.append(DOT);
            result.append(childAttribute);
        }

        return result.toString();
    }

    private Path<Object> getPathObject(Root rootEntry, String queryPath) {
        String[] items = queryPath.split(DOT_PATTERN);

        Path<Object> pathObject = null;
        if (items.length >= 1) {
            pathObject = rootEntry.get(items[0]);
            for (int i = 1; i < items.length; i++) {
                pathObject = pathObject.get(items[i]);
            }
        }
        return pathObject;
    }

    @Override
    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);
        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);

        String orderByColumn = getIdAttribute(entityClass);
        addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        return logAndReturnQueryResult("findWithPaging", entityClass, allQuery);
    }

    @Override
    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput, Map<String, Object> matchValues) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);

        List<Predicate> predicates = new ArrayList<>();
        getPredicates(predicates, matchValues, criteriaBuilder, rootEntry, true);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);

        String orderByColumn = getIdAttribute(entityClass);
        addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        return logAndReturnQueryResult("findWithPaging", entityClass, allQuery);
    }

    @Override
    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput, List<String> columnNames) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);
        List<Selection<?>> selections = new ArrayList<Selection<?>>();

        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalStateException("Select column names cannot be empty");
        }

        for (String columnName : columnNames) {
            selections.add(rootEntry.get(columnName));
        }

        CriteriaQuery<E> all = criteriaQuery.multiselect(selections);

        String orderByColumn = getIdAttribute(entityClass);
        addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);

        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        return logAndReturnQueryResult("findWithPaging", entityClass, allQuery);
    }

    private String getIdAttribute(Class entityClass) {
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                return field.getName();
            }
        }

        return null;
    }


    @Override
    public boolean isOpen() {
        return getEntityManager().isOpen();
    }


    @Override
    public Metamodel getMetaModel() {
        return getEntityManager().getMetamodel();
    }

    @Override
    public <E> boolean delete(E entity, Map values) {
        List<E> lists = findByMatchValue((Class<E>) entity.getClass(), values);
        for (E e : lists) {
            delete(e);
        }
        return true;
    }

    @Override
    public <E> boolean delete(Class<E> entityClass, Map values) {
        List<E> lists = findByMatchValue(entityClass, values);
        for (E e : lists) {
            delete(e);
        }
        return true;
    }

    @Override
    public <E> boolean deleteById(Class c, Object id) {
        Object e = findById(c, id);
        if (e != null) {
            LogUtil.logDebug(LOGGER, "deleteById called for : %s with id: %s", c, id);
            getEntityManager().remove(e);
            LogUtil.logDebug(LOGGER, "deleteById returned for : %s with id: %s", c, id);
        }
        return true;
    }

    @Override
    public int deleteAll(Class entityClass) {
        LogUtil.logDebug(LOGGER, "deleteAll called for : %s", entityClass);
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaDelete cd = cb.createCriteriaDelete(entityClass);
        cd.from(entityClass);
        Query query = getEntityManager().createQuery(cd);
        int removedNum = query.executeUpdate();
        LogUtil.logDebug(LOGGER, "deleteAll returned for : %s", entityClass);
        return removedNum;
    }

    @Override
    public <E> List<E> findLike(Class<E> entity, Map<String, String> matchValue) {
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery cq = cb.createQuery(entity);
        Root<E> rootEntry = cq.from(entity);

        for (Entry<String, String> entry : matchValue.entrySet()) {
            PredicateCondition.LIKE.addLike(cb, cq, rootEntry, entry.getKey(), entry.getValue());
        }

        CriteriaQuery<E> all = cq.select(rootEntry);
        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        
        allQuery.setLockMode(LockModeType.PESSIMISTIC_READ);

        return allQuery.getResultList();
    }
    
    @Override
    public <E> List<E> findLikeWithPagingInput(Class<E> entity, Map<String, String> matchValues, Map<String, String> likeValues,
            PagingInput pagingInput) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entity);
        Root<E> rootEntry = criteriaQuery.from(entity);
        
        List<Predicate> andPredicates = new ArrayList<>();
        if (matchValues != null) {
            for (Entry<String, String> entry : matchValues.entrySet()) {
                Path<Object> pathObject = getPathObject(rootEntry, entry.getKey());
                if (pathObject == null){
                    pathObject = rootEntry.get(entry.getKey());
                }
                Predicate predicate = criteriaBuilder.equal(pathObject, ConvertUtils.convert(entry.getValue(), pathObject.getJavaType()));
                andPredicates.add(predicate);
            }
        }

        List<Predicate> orPredicates = new ArrayList<>();
        if (likeValues != null) {
            for (Entry<String, String> entry : likeValues.entrySet()) {
                Path<Object> pathObject = getPathObject(rootEntry, entry.getKey());
                if (pathObject == null){
                    pathObject = rootEntry.get(entry.getKey());
                }
                Predicate predicate = getOrPredicate(entry.getValue(), criteriaBuilder, pathObject);
                orPredicates.add(predicate);
            }
        }
        
        Predicate orPredicate  = criteriaBuilder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
        andPredicates.add(orPredicate);
        
        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));
        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);

        String orderByColumn = getIdAttribute(entity);
        addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);


        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        return logAndReturnQueryResult("findByLike", entity, allQuery);
    }
    
    @Override
    public <E> List<E> findByMatchMultiConditions(Class<E> entity, List<Map<String, Object>> likeValues, PagingInput pagingInput) {
        return findByMatchMultiConditions(entity, likeValues, pagingInput, null);
    }
    
    @Override
    public <E> List<E> findByMatchMultiConditions(Class<E> entity, List<Map<String, Object>> likeValues, PagingInput pagingInput, String orderBy) {
        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entity);
        Root<E> rootEntry = criteriaQuery.from(entity);
        
        List<Predicate> andPredicates = new ArrayList<>();
        
        if (likeValues != null) {
            for (Map<String, Object> mapEntry : likeValues) {
                List<Predicate> orPredicates = new ArrayList<>();
                for(Entry<String, Object> entry : mapEntry.entrySet()){
                    if(entry.getValue() instanceof String){
                        Predicate predicate = getOrPredicate((String)entry.getValue(), criteriaBuilder, rootEntry.get(entry.getKey()));
                        orPredicates.add(predicate);
                    }else{
                        List<String> conditions = (List<String>)entry.getValue();
                        for(int i = 0; i < conditions.size(); i++){
                            Predicate predicate = criteriaBuilder.equal(rootEntry.get(entry.getKey()), conditions.get(i));
                            orPredicates.add(predicate);
                        }
                    }
                }
                Predicate orPredicate  = criteriaBuilder.or(orPredicates.toArray(new Predicate[orPredicates.size()]));
                andPredicates.add(orPredicate);
            }
        }
        
        criteriaQuery.where(andPredicates.toArray(new Predicate[andPredicates.size()]));
        CriteriaQuery<E> all = criteriaQuery.select(rootEntry);

        if (orderBy != null && !orderBy.isEmpty()) {
            addOrderByAsc(all, criteriaBuilder, rootEntry, orderBy);
        } else {
            addOrderByAsc(all, criteriaBuilder, rootEntry, getIdAttribute(entity));
        }

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);
        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        return logAndReturnQueryResult("findByMatchMultiConditions", entity, allQuery);
    }
    
    private <E> Predicate getOrPredicate(String value, CriteriaBuilder criteriaBuilder, Path columnPath) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("%");
        stringBuilder.append(value.toLowerCase());
        stringBuilder.append("%");
        Predicate predicate = criteriaBuilder.like(criteriaBuilder.lower(columnPath), stringBuilder.toString());
        return predicate;
    }

    @Override
    public <E> boolean deleteLike(Class<E> entity, Map likeValues) {
        List<E> likeList = findLike(entity, likeValues);
        for (E e : likeList) {
            delete(e);
        }
        return true;
    }

    @Override
    public <E> List<E> findWithPagingAndOrderByColumn(Class<E> entityClass, PagingInput pagingInput, Map<String, Object> matchValues,
                                                      String orderByColumn, Boolean isDesc) {
        TypedQuery<E> allQuery = findWithCriterias(entityClass, pagingInput, matchValues, null, null, null, orderByColumn, isDesc);
        return logAndReturnQueryResult("findWithPagingAndOrderByColumn", entityClass, allQuery);
    }

    @Override
    public <E> List<E> findSelectedColumnsWithMatchedValues(Class<E> entityClass, PagingInput pagingInput,
                                                            Map<String, Object> matchedValues, List<String> selectedAttrs) {
        TypedQuery<E> allQuery = findWithCriterias(entityClass, pagingInput, matchedValues, selectedAttrs, null, null, null, null);
        return logAndReturnQueryResult("findSelectedColumnsWithMatchedValues", entityClass, allQuery);
    }

    @Override
    public List<Object[]> findByNativeQuery(String query) {
        return getEntityManager().createNativeQuery(query).getResultList();
    }

    @Override
    public List<Object[]> findByQuery(String query) {
        return getEntityManager().createQuery(query).getResultList();
    }

    @Override
    public <E> E findFirstObjectByQuery(Class<E> entityClass, String query) {
        return getEntityManager().createQuery(query, entityClass).setFirstResult(0).setMaxResults(1).getSingleResult();
    }

    private <E> TypedQuery<E> findWithCriterias(Class<E> entityClass, PagingInput pagingInput, Map<String, Object> matchedValues,
                                                List<String> selectedAttrs, List<String> groupByAttrs, Boolean countByGroup, String
                                                        orderByColumn, Boolean isDesc) {

        CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<E> rootEntry = criteriaQuery.from(entityClass);

        /*
         * Start for 'where'
         */
        List<Predicate> predicates = new ArrayList<>();
        getPredicates(predicates, matchedValues, criteriaBuilder, rootEntry, true);

        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));
        // End for 'where' criteria

        /*
         * Start for 'select'
         */
        CriteriaQuery<E> all = null;
        if (selectedAttrs != null) {
            List<Selection<?>> selections = new ArrayList<Selection<?>>();
            for (String columnName : selectedAttrs) {
                selections.add(rootEntry.get(columnName));
            }
            if (countByGroup != null && countByGroup) {
                Expression<Long> count = criteriaBuilder.count(rootEntry);
                selections.add(count.alias("count"));
            }
            all = criteriaQuery.multiselect(selections);
        } else {
            all = criteriaQuery.select(rootEntry);
        }

        /*
         * Start for 'order by'
         */
        if (isDesc != null && isDesc) {
            addOrderByDesc(all, criteriaBuilder, rootEntry, orderByColumn);
        } else {
            addOrderByAsc(all, criteriaBuilder, rootEntry, orderByColumn);
        }
        // End for 'order by'

        /*
         * Start for 'group by'
         */
        if (groupByAttrs != null) {
            for (String attrName : groupByAttrs) {
                Expression<String> attrExpression = rootEntry.get(attrName);
                criteriaQuery.groupBy(attrExpression);
            }
        }
        // End for 'group by'

        TypedQuery<E> allQuery = getEntityManager().createQuery(all);

        /*
         * Start for 'limit'
         */
        if (pagingInput != null) {
            allQuery.setFirstResult(pagingInput.getFirstResult());
            allQuery.setMaxResults(pagingInput.getMaxResult());
        }
        // End for 'limit'

        return allQuery;
    }

    public <E> void lock(E object, LockModeType lockModeType) {
        getEntityManager().lock(object, lockModeType);
    }

    public <E> void lock(E object) {
        lock(object, LockModeType.PESSIMISTIC_WRITE);
    }

    @Override
    public void dumpModifiedSessionVariables() {
        // do nothing
    }

    @Override
    public void setCustomDbEnvValues() {
        //do nothing
    }
}
