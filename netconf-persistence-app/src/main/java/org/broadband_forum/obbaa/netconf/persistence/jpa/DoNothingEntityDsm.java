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

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.CollectionAttribute;
import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.MapAttribute;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.PluralAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;

public class DoNothingEntityDsm extends AbstractEntityDataStoreManager {
    private EntityManager m_entityManager;

    public DoNothingEntityDsm(){
        m_entityManager = new DoNothingEM();
    }

    @Override
    public void beginTransaction() {

    }

    @Override
    public void commitTransaction() {

    }

    @Override
    public void rollbackTransaction() {

    }

    @Override
    public void close() {

    }

    @Override
    public <E> E findById(Class<E> entityClass, Object primaryKey, LockModeType lockMode) {
        return (E) new Object();
    }

    @Override
    public EntityManager getEntityManager() {
        return m_entityManager;
    }

    private class DoNothingEM implements EntityManager {
        @Override
        public void persist(Object o) {

        }

        @Override
        public <T> T merge(T t) {
            return null;
        }

        @Override
        public void remove(Object o) {

        }

        @Override
        public <T> T find(Class<T> aClass, Object o) {
            return null;
        }

        @Override
        public <T> T find(Class<T> aClass, Object o, Map<String, Object> map) {
            return null;
        }

        @Override
        public <T> T find(Class<T> aClass, Object o, LockModeType lockModeType) {
            return null;
        }

        @Override
        public <T> T find(Class<T> aClass, Object o, LockModeType lockModeType, Map<String, Object> map) {
            return null;
        }

        @Override
        public <T> T getReference(Class<T> aClass, Object o) {
            return null;
        }

        @Override
        public void flush() {

        }

        @Override
        public void setFlushMode(FlushModeType flushModeType) {

        }

        @Override
        public FlushModeType getFlushMode() {
            return null;
        }

        @Override
        public void lock(Object o, LockModeType lockModeType) {

        }

        @Override
        public void lock(Object o, LockModeType lockModeType, Map<String, Object> map) {

        }

        @Override
        public void refresh(Object o) {

        }

        @Override
        public void refresh(Object o, Map<String, Object> map) {

        }

        @Override
        public void refresh(Object o, LockModeType lockModeType) {

        }

        @Override
        public void refresh(Object o, LockModeType lockModeType, Map<String, Object> map) {

        }

        @Override
        public void clear() {

        }

        @Override
        public void detach(Object o) {

        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public LockModeType getLockMode(Object o) {
            return null;
        }

        @Override
        public void setProperty(String s, Object o) {

        }

        @Override
        public Map<String, Object> getProperties() {
            return null;
        }

        @Override
        public Query createQuery(String s) {
            return null;
        }

        @Override
        public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
            return null;
        }

        @Override
        public Query createQuery(CriteriaUpdate criteriaUpdate) {
            return null;
        }

        @Override
        public Query createQuery(CriteriaDelete criteriaDelete) {
            return null;
        }

        @Override
        public <T> TypedQuery<T> createQuery(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public Query createNamedQuery(String s) {
            return null;
        }

        @Override
        public <T> TypedQuery<T> createNamedQuery(String s, Class<T> aClass) {
            return null;
        }

        @Override
        public Query createNativeQuery(String s) {
            return null;
        }

        @Override
        public Query createNativeQuery(String s, Class aClass) {
            return null;
        }

        @Override
        public Query createNativeQuery(String s, String s1) {
            return null;
        }

        @Override
        public StoredProcedureQuery createNamedStoredProcedureQuery(String s) {
            return null;
        }

        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String s) {
            return null;
        }

        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String s, Class... classes) {
            return null;
        }

        @Override
        public StoredProcedureQuery createStoredProcedureQuery(String s, String... strings) {
            return null;
        }

        @Override
        public void joinTransaction() {

        }

        @Override
        public boolean isJoinedToTransaction() {
            return false;
        }

        @Override
        public <T> T unwrap(Class<T> aClass) {
            return null;
        }

        @Override
        public Object getDelegate() {
            return null;
        }

        @Override
        public void close() {

        }

        @Override
        public boolean isOpen() {
            return false;
        }

        @Override
        public EntityTransaction getTransaction() {
            return null;
        }

        @Override
        public EntityManagerFactory getEntityManagerFactory() {
            return null;
        }

        @Override
        public CriteriaBuilder getCriteriaBuilder() {
            return null;
        }

        @Override
        public Metamodel getMetamodel() {
            return new Metamodel() {
                @Override
                public <X> EntityType<X> entity(Class<X> aClass) {
                    return new EntityType<X>() {

                        @Override
                        public <Y> SingularAttribute<? super X, Y> getId(Class<Y> type) {
                            return null;
                        }

                        @Override
                        public <Y> SingularAttribute<X, Y> getDeclaredId(Class<Y> type) {
                            return null;
                        }

                        @Override
                        public <Y> SingularAttribute<? super X, Y> getVersion(Class<Y> type) {
                            return null;
                        }

                        @Override
                        public <Y> SingularAttribute<X, Y> getDeclaredVersion(Class<Y> type) {
                            return null;
                        }

                        @Override
                        public IdentifiableType<? super X> getSupertype() {
                            return null;
                        }

                        @Override
                        public boolean hasSingleIdAttribute() {
                            return false;
                        }

                        @Override
                        public boolean hasVersionAttribute() {
                            return false;
                        }

                        @Override
                        public Set<SingularAttribute<? super X, ?>> getIdClassAttributes() {
                            return null;
                        }

                        @Override
                        public Type<?> getIdType() {
                            return new org.hibernate.metamodel.model.domain.internal.BasicTypeImpl<>(String.class, PersistenceType.BASIC);
                        }

                        @Override
                        public Set<Attribute<? super X, ?>> getAttributes() {
                            return null;
                        }

                        @Override
                        public Set<Attribute<X, ?>> getDeclaredAttributes() {
                            return null;
                        }

                        @Override
                        public <Y> SingularAttribute<? super X, Y> getSingularAttribute(String name, Class<Y> type) {
                            return null;
                        }

                        @Override
                        public <Y> SingularAttribute<X, Y> getDeclaredSingularAttribute(String name, Class<Y> type) {
                            return null;
                        }

                        @Override
                        public Set<SingularAttribute<? super X, ?>> getSingularAttributes() {
                            return null;
                        }

                        @Override
                        public Set<SingularAttribute<X, ?>> getDeclaredSingularAttributes() {
                            return null;
                        }

                        @Override
                        public <E> CollectionAttribute<? super X, E> getCollection(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <E> CollectionAttribute<X, E> getDeclaredCollection(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <E> SetAttribute<? super X, E> getSet(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <E> SetAttribute<X, E> getDeclaredSet(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <E> ListAttribute<? super X, E> getList(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <E> ListAttribute<X, E> getDeclaredList(String name, Class<E> elementType) {
                            return null;
                        }

                        @Override
                        public <K, V> MapAttribute<? super X, K, V> getMap(String name, Class<K> keyType,
                                Class<V> valueType) {
                            return null;
                        }

                        @Override
                        public <K, V> MapAttribute<X, K, V> getDeclaredMap(String name, Class<K> keyType,
                                Class<V> valueType) {
                            return null;
                        }

                        @Override
                        public Set<PluralAttribute<? super X, ?, ?>> getPluralAttributes() {
                            return null;
                        }

                        @Override
                        public Set<PluralAttribute<X, ?, ?>> getDeclaredPluralAttributes() {
                            return null;
                        }

                        @Override
                        public Attribute<? super X, ?> getAttribute(String name) {
                            return null;
                        }

                        @Override
                        public Attribute<X, ?> getDeclaredAttribute(String name) {
                            return null;
                        }

                        @Override
                        public SingularAttribute<? super X, ?> getSingularAttribute(String name) {
                            return null;
                        }

                        @Override
                        public SingularAttribute<X, ?> getDeclaredSingularAttribute(String name) {
                            return null;
                        }

                        @Override
                        public CollectionAttribute<? super X, ?> getCollection(String name) {
                            return null;
                        }

                        @Override
                        public CollectionAttribute<X, ?> getDeclaredCollection(String name) {
                            return null;
                        }

                        @Override
                        public SetAttribute<? super X, ?> getSet(String name) {
                            return null;
                        }

                        @Override
                        public SetAttribute<X, ?> getDeclaredSet(String name) {
                            return null;
                        }

                        @Override
                        public ListAttribute<? super X, ?> getList(String name) {
                            return null;
                        }

                        @Override
                        public ListAttribute<X, ?> getDeclaredList(String name) {
                            return null;
                        }

                        @Override
                        public MapAttribute<? super X, ?, ?> getMap(String name) {
                            return null;
                        }

                        @Override
                        public MapAttribute<X, ?, ?> getDeclaredMap(String name) {
                            return null;
                        }

                        @Override
                        public PersistenceType getPersistenceType() {
                            return null;
                        }

                        @Override
                        public Class<X> getJavaType() {
                            return null;
                        }

                        @Override
                        public BindableType getBindableType() {
                            return null;
                        }

                        @Override
                        public Class<X> getBindableJavaType() {
                            return null;
                        }

                        @Override
                        public String getName() {
                            return null;
                        }
                        
                    };
                }

                @Override
                public <X> ManagedType<X> managedType(Class<X> aClass) {
                    return null;
                }

                @Override
                public <X> EmbeddableType<X> embeddable(Class<X> aClass) {
                    return null;
                }

                @Override
                public Set<ManagedType<?>> getManagedTypes() {
                    return null;
                }

                @Override
                public Set<EntityType<?>> getEntities() {
                    return null;
                }

                @Override
                public Set<EmbeddableType<?>> getEmbeddables() {
                    return null;
                }
            };
        }

        @Override
        public <T> EntityGraph<T> createEntityGraph(Class<T> aClass) {
            return null;
        }

        @Override
        public EntityGraph<?> createEntityGraph(String s) {
            return null;
        }

        @Override
        public EntityGraph<?> getEntityGraph(String s) {
            return null;
        }

        @Override
        public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> aClass) {
            return null;
        }
    }
}
