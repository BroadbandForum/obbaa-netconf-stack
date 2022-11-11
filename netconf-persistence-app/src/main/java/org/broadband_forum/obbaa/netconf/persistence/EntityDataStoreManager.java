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

package org.broadband_forum.obbaa.netconf.persistence;

import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.metamodel.Metamodel;

import org.broadband_forum.obbaa.netconf.persistence.jpa.PredicateCondition;

public interface EntityDataStoreManager {
    public static final String PARENT_KEY = "parentId";

    @Deprecated
    public void beginTransaction();

    @Deprecated
    public void commitTransaction();

    @Deprecated
    public void rollbackTransaction();

    public void close();

    public <E> boolean create(E entity);

    public <E> E findById(Class<E> entityClass, Object primaryKey);

    public <E> E findById(Class<E> entityClass, Object primaryKey, LockModeType lockMode);

    public <E> E findByUniqueKeys(Class<E> entityClass, Map<String, Object> key);

    public <E> List<E> findAll(Class<E> entityClass);

    public <E> List<E> findAll(Class<E> pojoClass, String orderByColumn);

    public <E> List<E> findAll(Class<E> pojoClass, List<String> orderByColumn);
 
    public <E> List<E> findRange(Class<E> pojoClass, String predicateColumn, PredicateCondition condition, Double value);

    public <E> List<E> findRangeBetween(Class<E> pojoClass, String predicateColumn, Double lowerLimit, Double upperLimit);

    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput);

    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput, List<String> columnNames);

    public <E> List<E> findWithPaging(Class<E> entityClass, PagingInput pagingInput, Map<String, Object> matchValues);

    public <E> List<E> findByMatchValue(Class<E> entityClass, Map<String, Object> matchValues);

    public <E> List<E> findByMatchValue(Class<E> entityClass,Map<String, Object> matchValues, LockModeType lockModeType);

    public <E> List<E> findByMatchValue(Class<E> pojoClass, Map<String, Object> matchValues, String orderByColumn);

    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues, Map<String, Object>
            notMatchValues, LockModeType lockModeType);

    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues, Map<String, Object>
            notMatchValues, String orderByColumn, LockModeType lockModeType);

    public <E> List<E> findByMatchAndNotMatchValue(Class<E> entityClass, Map<String, Object> matchValues, Map<String, Object>
            notMatchValues, String orderByColumn, boolean isDesc, LockModeType lockModeType);

    public <E> List<E> findByMatchAndNotMatchValues(Class<E> entityClass, Map<String, List<Object>> matchValues, Map<String,
            List<Object>> notMatchValues);

    public <E> List<E> findByMatchAndNotMatchValues(Class<E> entityClass, Map<String, List<Object>> matchValues, Map<String,
            List<Object>> notMatchValues, String orderByColumn);

    public <E> List<E> findByIsNotNull(Class<E> entityClass, List<String> attributes);

    public <E> List<E> findLike(Class<E> entity, Map<String, String> matchValue);
    
    public <E> List<E> findLikeWithPagingInput(Class<E> entity, Map<String, String> matchValues, Map<String, String> likeValues, PagingInput pagingInput);

    public <E> List<E> findWithPagingAndOrderByColumn(Class<E> entityClass, PagingInput pagingInput, Map<String, Object> matchValues,
                                                      String orderByColumn, Boolean isDesc);

    public Long countByMatchAndNotMatchValue(Class entityClass, Map<String, Object> matchValues, Map<String, Object> notMatchValues);
    
    public Long countByMatchAndLikeValue(Class entityClass, Map<String, String> matchValues, Map<String, String> likeValues, LockModeType lockMode);

    public Long countByMatchAndNotMatchValue(Class entityClass, Map<String, Object> matchValues, Map<String, Object> notMatchValues, LockModeType lockMode);
    
    public <E> List<E> findByMatchMultiConditions(Class<E> entity, List<Map<String, Object>> likeValues, PagingInput pagingInput);
    
    public <E> List<E> findByMatchMultiConditions(Class<E> entity, List<Map<String, Object>> likeValues, PagingInput pagingInput, String orderBy);

    public Long countByMatchMultiConditions(Class entity, List<Map<String, Object>> likeValues);

    public <E> boolean merge(E entity);

    public <E> boolean delete(E entity, Map values);

    public <E> boolean delete(Class<E> entity, Map values);

    public <E> boolean deleteLike(Class<E> entity, Map likeValues);

    public <E> boolean deleteById(Class c, Object id);

    public int deleteAll(Class entityClass);

    /**
     * Use only when an entity is found and delete is called in the same tx. If not use
     * <p>
     * {@link #delete(Object, Map)} instead
     */
    public <E> boolean delete(E entity);

    public void flush();

    public boolean isOpen();

    //FIXME: FNMS-10118 Exposing the entity manager must be avoided. Paves way for uncontrolled life-cycle of EM!!
    public EntityManager getEntityManager();

    public Metamodel getMetaModel();

    public <E> List<E> findSelectedColumnsWithMatchedValues(Class<E> entityClass, PagingInput pagingInput,
                                                            Map<String, Object> matchedValues, List<String> selectedAttrs);

    public List<Object[]> findByNativeQuery(String query);

    public List<Object[]> findByQuery(String query);

    /*
     * (non-Javadoc)
     * 
     * @see org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager#findFirstObjectByQuery(java.lang.Class, java.lang.String) The select list
     * of the query must contain only a single item
     * 
     * @param qlString a Java Persistence query string
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws NoResultException if no result is found
     * @throws IllegalArgumentException if the query string is found to be invalid or if the query result is found to not be assignable to
     * the specified type
     */
    public <E> E findFirstObjectByQuery(Class<E> entityClass, String query);

    <E> List<E> findByLike(Class<E> entityClass, String columnName, String value);

    <E> List<E> findByLike(Class<E> entityClass, String columnName, String value, int count);

    <E> List<E> findByLike(Class klass, Map<String, String> columnKeyMap, Map<String, Object> matchValues, int maxResults);

    <E> List<E> findByLikeWithFieldsContain(Class klass, Map<String, String> columnKeyMap, Map<String, Object> parentIdMatchMap, int maxResults);

    public <E> void lock(E entity, LockModeType lockMode);

    public <E> void lock(E entity);

    void dumpModifiedSessionVariables();

    void setCustomDbEnvValues();

    void persist(Object entity);

    <E> List<E> findObjectByQuery(Class<E> entityClass, String query);

}
