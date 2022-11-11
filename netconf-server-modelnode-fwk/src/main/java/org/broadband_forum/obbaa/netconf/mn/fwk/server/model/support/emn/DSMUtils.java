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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.endPhase;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DSMTimingLogger.startPhase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.hibernate.proxy.HibernateProxy;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DSMUtils {

    static Map<QName, LinkedHashSet<ConfigLeafAttribute>> getConfigLeafListsFromEntity(SchemaRegistry schemaRegistry, SchemaPath schemaPath,
                                                                                       EntityRegistry entityRegistry, Class<?> klass, Object entity) {
        Map<QName, LinkedHashSet<ConfigLeafAttribute>> leafLists = new HashMap<>();
        Map<QName, Method> leafListGetters = entityRegistry.getYangLeafListGetters(klass);
        for(Map.Entry<QName, Method> leafListGetter : leafListGetters.entrySet()){
            Set<Object> values;
            try{
                values = (Set<Object>) leafListGetter.getValue().invoke(entity);
                if (values!=null && !values.isEmpty()) {
                    QName leafListQName = leafListGetter.getKey();
                    boolean leafListOrderedByUser = SchemaRegistryUtil.isLeafListOrderedByUser(leafListQName, schemaPath, schemaRegistry);
                    LinkedHashSet<ConfigLeafAttribute> leafList = new LinkedHashSet<>();

                    Iterator iterator = values.iterator();
                    while (iterator.hasNext()){
                        Object leafListEntity = iterator.next();
                        Class<?> leafListKlass = getEntityClass(leafListEntity);
                        if (leafListKlass!=null) {
                            Map<QName, Method> leafListEntityAttrGetters = entityRegistry.getAttributeGetters(leafListKlass);
                            Method attributeGetter = leafListEntityAttrGetters.get(leafListQName);
                            String leafListStringValue = (String) attributeGetter.invoke(leafListEntity);

                            Map<QName, Method> leafListEntityAttrNsGetters = entityRegistry.getYangAttributeNSGetters(leafListKlass);
                            Method identityRefNSGetter= leafListEntityAttrNsGetters.get(leafListQName);

                            ConfigLeafAttribute configLeafAttribute;
                            if(identityRefNSGetter!=null){
                                String leafListStringNsValue = (String) identityRefNSGetter.invoke(leafListEntity);
                                configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry,
                                        schemaPath,leafListStringNsValue, leafListQName,leafListStringValue);
                            }else{
                                configLeafAttribute = ConfigAttributeFactory.getConfigAttributeFromEntity(schemaRegistry,
                                        schemaPath, leafListQName.getLocalName(), leafListQName, leafListStringValue);
                            }
                            if (leafListOrderedByUser) {
                                Integer insertIndex = getInsertIndex(leafListEntity, leafListKlass, entityRegistry);
                                configLeafAttribute.setInsertIndex(insertIndex);
                            }
                            leafList.add(configLeafAttribute);
                        }
                    }
                    leafLists.put(leafListQName, leafList);
                }
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new ModelNodeMapperException(e);
            }
        }
        return leafLists;
    }

    public static Class<?> getEntityClass(Object entityObject) {
        if (entityObject instanceof HibernateProxy) {
            return ((HibernateProxy) entityObject).getHibernateLazyInitializer()
                    .getPersistentClass();
        }
        return entityObject.getClass();
    }

    private static Integer getInsertIndex(Object leafListEntity, Class leafListKlass, EntityRegistry entityRegistry) {
        Method orderByUserGetter = entityRegistry.getOrderByUserGetter(leafListKlass);
        if(orderByUserGetter != null){
            try {
                return (Integer) orderByUserGetter.invoke(leafListEntity);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        throw new RuntimeException("Leaf-list is ordered-by-user but insertIndex not stored ");
    }

    public static void setVisibility(EntityRegistry entityRegistry, Object entity, HelperDrivenModelNode modelNode) {
        Method yangVisibilityControllerGetter = entityRegistry.getYangVisibilityControllerGetter(getEntityClass(entity));
        if(yangVisibilityControllerGetter != null) {
            try {
                boolean value = (boolean) yangVisibilityControllerGetter.invoke(entity);
                modelNode.setVisibility(value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String getXmlSubtree(Object parentEntity, Class parentKlass, EntityRegistry entityRegistry) throws DataStoreException {
        try {
            startPhase("XmlSubtreeDSM.getXmlSubtree");
            Method yangXmlSubtreeGetter = entityRegistry.getYangXmlSubtreeGetter(parentKlass);
            String yangXmlSubtree = "";
            if (yangXmlSubtreeGetter != null) {
                try {
                    yangXmlSubtree = (String) yangXmlSubtreeGetter.invoke(parentEntity);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return yangXmlSubtree;
        } finally {
            endPhase("XmlSubtreeDSM.getXmlSubtree", false);
        }
    }
}
