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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationUtil;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Created by keshava on 4/12/15.
 */
public class EntityRegistryBuilder {

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(EntityRegistryBuilder.class,
            "netconf-stack", "DEBUG", "GLOBAL");

    private static final String NAME = "name";
    private static final String NAMESPACE = "namespace";
    private static final String REVISION = "revision";

    public static void updateEntityRegistry(String componentId, List<Class> entityClasses, EntityRegistry
            entityRegistry,
                                            SchemaRegistry schemaRegistry, EntityDataStoreManager entityDSM,
                                            ModelNodeDSMRegistry modelNodeDSMRegistry) throws
            AnnotationAnalysisException {
        List<Class> rootClasses = getRootClasses(entityClasses);
        if (rootClasses.isEmpty()) {
            throw new AnnotationAnalysisException("The list of classes must contain at-least one root");
        }
        for (Class rootClass : rootClasses) {
            updateSchemaPaths(componentId, rootClass, null, entityRegistry, schemaRegistry, entityDSM,
                    modelNodeDSMRegistry);
        }
    }

    private static void updateSchemaPaths(String componentId, Class subrootClass, SchemaPath parentSchemaPath,
                                          EntityRegistry entityRegistry,
                                          SchemaRegistry schemaRegistry, EntityDataStoreManager entityDSM,
                                          ModelNodeDSMRegistry modelNodeDSMRegistry) {
        QName klassQName = null;
        if (parentSchemaPath != null) {
            klassQName = getQName(subrootClass, parentSchemaPath.getLastComponent());
        } else {
            List<SchemaPath> parents = new LinkedList<>();
            for (Method method : subrootClass.getDeclaredMethods()) {
                if (Modifier.isStatic(method.getModifiers()) && Modifier.isPublic(method.getModifiers())) {
                    YangParentSchemaPath parentAnnotation = method.getAnnotation(YangParentSchemaPath.class);
                    if (parentAnnotation != null && SchemaPath.class.equals(method.getReturnType())) {
                        try {
                            SchemaPath parentPath = (SchemaPath) method.invoke(null);
                            if (parentPath != null) {
                                parents.add(parentPath);
                            }
                        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                            LOGGER.warn(String.format("Couldn't get Parent SchemaPath from Annotation in Class [%s]",
                                    subrootClass.getName()), e);
                        }
                    }
                }
            }
            if (parents.isEmpty()) {
                klassQName = getQName(subrootClass, null);
            } else {
                for (SchemaPath parentPath : parents) {
                    updateSchemaPaths(componentId, subrootClass, parentPath, entityRegistry, schemaRegistry,
                            entityDSM, modelNodeDSMRegistry);
                }
                return;
            }
        }
        Map<SchemaPath, Class> schemaPaths = new HashMap<>();
        SchemaPath schemaPath = schemaRegistry.getDescendantSchemaPath(parentSchemaPath, klassQName);
        if (schemaPath == null) {
            schemaPath = new SchemaPathBuilder().withParent(parentSchemaPath).appendQName(klassQName).build();
        }
        schemaPaths.put(schemaPath, subrootClass);
        entityRegistry.addSchemaPaths(componentId, schemaPaths);
        if (entityDSM != null) {
            modelNodeDSMRegistry.addEntityDSM(subrootClass, entityDSM);
        }
        entityRegistry.addComponentClass(componentId, subrootClass);
        entityRegistry.addYangSchemaPathSetter(subrootClass, getYangSchemaPathSetter(subrootClass));
        entityRegistry.addYangSchemaPathGetter(subrootClass, getYangSchemaPathGetter(subrootClass));
        for (Field field : subrootClass.getDeclaredFields()) {
            YangChild annotation = field.getAnnotation(YangChild.class);
            if (annotation != null) {
                Type type = field.getType();
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    type = parameterizedType.getActualTypeArguments()[0];
                }
                QName childQname = getQName((Class) type, klassQName);
                entityRegistry.addYangChildGetters(subrootClass, getYangChildGetters(subrootClass, childQname, field));
                entityRegistry.addYangChildSetters(subrootClass, getYangChildSetters(subrootClass, childQname, field));
                updateSchemaPaths(componentId, (Class) type, schemaPath, entityRegistry, schemaRegistry, entityDSM,
                        modelNodeDSMRegistry);
            }
            YangLeafList leafListAnnotation = field.getAnnotation(YangLeafList.class);
            if (leafListAnnotation != null) {
                Type type = field.getType();
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();
                    type = parameterizedType.getActualTypeArguments()[0];
                }
                entityRegistry.addYangLeafListGetters(subrootClass, getYangLeafListGetters(subrootClass, klassQName));
                entityRegistry.addYangLeafListSetters(subrootClass, getYangLeafListSetters(subrootClass, klassQName));
                updateSchemaPaths(componentId, (Class) type, schemaPath, entityRegistry, schemaRegistry, entityDSM,
                        modelNodeDSMRegistry);
            }
        }
        entityRegistry.addConfigAttributeGetters(subrootClass, getConfigAttributeGetters(subrootClass, klassQName),
                getFieldQNames(subrootClass, klassQName));
        entityRegistry.addConfigAttributeSetters(subrootClass, getConfigAttributeSetters(subrootClass, klassQName),
                getParentIdSetter(subrootClass));
        entityRegistry.addParentIdGetter(subrootClass, getParentIdGetter(subrootClass));
        entityRegistry.addOrderByUserGetter(subrootClass, getOrderByUserGetters(subrootClass));
        entityRegistry.addOrderByUserSetter(subrootClass, getOrderByUserSetters(subrootClass));
        entityRegistry.addYangLeafListGetters(subrootClass, getYangLeafListGetters(subrootClass, klassQName));
        entityRegistry.addYangLeafListSetters(subrootClass, getYangLeafListSetters(subrootClass, klassQName));
        entityRegistry.addYangAttributeNSGetters(subrootClass, getYangIdentityRefNSGetters(subrootClass, klassQName));
        entityRegistry.addYangAttributeNSSetters(subrootClass, getYangIdentityRefNSSetters(subrootClass, klassQName));
        Method yangXmlSubtreeGetter = getYangXmlSubtreeGetter(subrootClass);
        if (yangXmlSubtreeGetter != null) {
            entityRegistry.addYangXmlSubtreeGetter(subrootClass, yangXmlSubtreeGetter);
        }
        Method yangXmlSubtreeSetter = getYangXmlSubtreeSetter(subrootClass);
        if (yangXmlSubtreeSetter != null) {
            entityRegistry.addYangXmlSubtreeSetter(subrootClass, yangXmlSubtreeSetter);
        }
    }

    private static Map<QName, Method> getYangIdentityRefNSGetters(Class klass, QName klassQName) {
        Map<QName, Method> getters = new HashMap<>();
        for (Field attributeField : klass.getDeclaredFields()) {
            YangAttribute yangAttribute = attributeField.getAnnotation(YangAttribute.class);
            if (yangAttribute != null && (yangAttribute.attributeType().equals(AttributeType
                    .IDENTITY_REF_CONFIG_ATTRIBUTE) ||
                    yangAttribute.attributeType().equals(AttributeType.INSTANCE_IDENTIFIER_CONFIG_ATTRIBUTE))) {
                for (Field identityRefField : klass.getDeclaredFields()) {
                    YangAttributeNS yangAttributeNS = identityRefField.getAnnotation(YangAttributeNS.class);
                    QName attributeQName = getQName(attributeField.getName(), attributeField.getDeclaredAnnotations()
                            , klassQName);
                    if (yangAttributeNS != null && attributeQName.getLocalName().equals(yangAttributeNS
                            .belongsToAttribute()) &&
                            attributeQName.getNamespace().toString().equals(yangAttributeNS.attributeNamespace()) &&
                            attributeQName.getFormattedRevision().equals(yangAttributeNS.attributeRevision())) {
                        addIdentityRefNSGetter(klass, klassQName, getters, identityRefField, attributeField);
                        break;
                    }
                }
            }
        }
        return getters;
    }

    private static void addIdentityRefNSGetter(Class klass, QName klassQName, Map<QName, Method> getters, Field
            identityRefNSField, Field
            identityRefAttribute) {
        Method getter;
        try {
            getter = AnnotationUtil.getGetMethod(klass, identityRefNSField);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        QName attrQname = getQName(identityRefAttribute.getName(), identityRefAttribute.getDeclaredAnnotations(),
                klassQName);
        getters.put(attrQname, getter); // getter method of NS field mapped to attribute QName
    }

    private static Map<QName, Method> getYangIdentityRefNSSetters(Class klass, QName klassQName) {
        Map<QName, Method> setters = new HashMap<>();
        for (Field attributeField : klass.getDeclaredFields()) {
            YangAttribute yangAttribute = attributeField.getAnnotation(YangAttribute.class);
            if (yangAttribute != null && (yangAttribute.attributeType().equals(AttributeType
                    .IDENTITY_REF_CONFIG_ATTRIBUTE) ||
                    yangAttribute.attributeType().equals(AttributeType.INSTANCE_IDENTIFIER_CONFIG_ATTRIBUTE))) {
                for (Field identityRefField : klass.getDeclaredFields()) {
                    YangAttributeNS yangAttributeNS = identityRefField.getAnnotation(YangAttributeNS.class);
                    QName attributeQName = getQName(attributeField.getName(), attributeField.getDeclaredAnnotations()
                            , klassQName);
                    if (yangAttributeNS != null && attributeQName.getLocalName().equals(yangAttributeNS
                            .belongsToAttribute()) &&
                            attributeQName.getNamespace().toString().equals(yangAttributeNS.attributeNamespace()) &&
                            attributeQName.getFormattedRevision().equals(yangAttributeNS.attributeRevision())) {
                        addIdentityRefNSSetter(klass, klassQName, setters, identityRefField, attributeField);
                        break;
                    }
                }
            }
        }
        return setters;
    }

    private static void addIdentityRefNSSetter(Class klass, QName klassQName, Map<QName, Method> setters, Field
            identityRefNSField, Field
            identityRefAttribute) {
        Method setter;
        try {
            setter = AnnotationUtil.getSetMethod(klass, identityRefNSField);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        QName attrQname = getQName(identityRefAttribute.getName(), identityRefAttribute.getDeclaredAnnotations(),
                klassQName);
        setters.put(attrQname, setter); // setter method of NS field mapped to attribute QName
    }

    private static Method getYangSchemaPathSetter(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangSchemaPath yangChildAnnotation = field.getAnnotation(YangSchemaPath.class);
            if (yangChildAnnotation != null) {
                try {
                    return AnnotationUtil.getSetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static Method getYangSchemaPathGetter(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangSchemaPath yangSchemaPathAnnotation = field.getAnnotation(YangSchemaPath.class);
            if (yangSchemaPathAnnotation != null) {
                try {
                    return AnnotationUtil.getGetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static Map<QName, Method> getYangChildGetters(Class klass, QName childQname, Field field) {
        Map<QName, Method> childGetters = new HashMap<>();
        YangChild yangChildAnnotation = field.getAnnotation(YangChild.class);
        if (yangChildAnnotation != null) {
            addChildGetter(klass, childGetters, field, childQname);
        }
        return childGetters;
    }

    private static Map<QName, Method> getYangChildSetters(Class klass, QName childQname, Field field) {
        Map<QName, Method> childSetters = new HashMap<>();
        YangChild yangChildAnnotation = field.getAnnotation(YangChild.class);
        if (yangChildAnnotation != null) {
            addChildSetter(klass, childSetters, field, childQname);
        }
        return childSetters;
    }

    private static Method getYangXmlSubtreeGetter(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangXmlSubtree yangXmlSubtreeAnnotation = field.getAnnotation(YangXmlSubtree.class);
            if (yangXmlSubtreeAnnotation != null) {
                try {
                    return AnnotationUtil.getGetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static Method getYangXmlSubtreeSetter(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangXmlSubtree yangXmlSubtreeAnnotation = field.getAnnotation(YangXmlSubtree.class);
            if (yangXmlSubtreeAnnotation != null) {
                try {
                    return AnnotationUtil.getSetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static Map<QName, Method> getConfigAttributeGetters(Class klass, QName klassQName) {
        Map<QName, Method> getters = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            YangAttribute yangAttributeAnnotation = field.getAnnotation(YangAttribute.class);

            if (yangAttributeAnnotation != null) {
                addGetter(klass, klassQName, getters, field);
            } else {
                YangListKey yangListKey = field.getAnnotation(YangListKey.class);
                if (yangListKey != null) {
                    addGetter(klass, klassQName, getters, field);
                }
            }
        }
        return getters;
    }

    private static Map<QName, Method> getConfigAttributeSetters(Class klass, QName klassQName) {
        Map<QName, Method> setters = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            YangAttribute yangAttributeAnnotation = field.getAnnotation(YangAttribute.class);

            if (yangAttributeAnnotation != null) {
                addSetter(klass, klassQName, setters, field);
            } else {
                YangListKey yangListKey = field.getAnnotation(YangListKey.class);
                if (yangListKey != null) {
                    addSetter(klass, klassQName, setters, field);
                }
            }
        }
        return setters;
    }

    private static Method getOrderByUserGetters(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangOrderByUser yangOrderByUserAnnotation = field.getAnnotation(YangOrderByUser.class);
            if (yangOrderByUserAnnotation != null) {
                try {
                    return AnnotationUtil.getGetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static Method getOrderByUserSetters(Class klass) {
        for (Field field : klass.getDeclaredFields()) {
            YangOrderByUser yangOrderByUserAnnotation = field.getAnnotation(YangOrderByUser.class);
            if (yangOrderByUserAnnotation != null) {
                try {
                    return AnnotationUtil.getSetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    private static void addChildGetter(Class klass, Map<QName, Method> childGetters, Field field, QName childQName) {
        Method childGetter = null;
        try {
            childGetter = AnnotationUtil.getGetMethod(klass, field);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        childGetters.put(childQName, childGetter);
    }

    private static void addChildSetter(Class klass, Map<QName, Method> childSetters, Field field, QName childQname) {
        Method childSetter = null;
        try {
            childSetter = AnnotationUtil.getSetMethod(klass, field);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        childSetters.put(childQname, childSetter);
    }

    private static Method getParentIdSetter(Class klass) {
        Method setter = null;
        for (Field field : klass.getDeclaredFields()) {
            YangParentId yangParentIdAnnotation = field.getAnnotation(YangParentId.class);
            if (yangParentIdAnnotation != null) {
                try {
                    setter = AnnotationUtil.getSetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        return setter;
    }

    private static Method getParentIdGetter(Class klass) {
        Method getter = null;
        for (Field field : klass.getDeclaredFields()) {
            YangParentId yangParentIdAnnotation = field.getAnnotation(YangParentId.class);
            if (yangParentIdAnnotation != null) {
                try {
                    getter = AnnotationUtil.getGetMethod(klass, field);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
        return getter;
    }

    private static Map<QName, String> getFieldQNames(Class klass, QName klassQName) {
        Map<QName, String> fieldNames = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            YangAttribute yangAttributeAnnotation = field.getAnnotation(YangAttribute.class);

            if (yangAttributeAnnotation != null) {
                addField(klassQName, fieldNames, field);
            } else {
                YangListKey yangListKey = field.getAnnotation(YangListKey.class);
                if (yangListKey != null) {
                    addField(klassQName, fieldNames, field);
                }
            }
        }
        return fieldNames;
    }

    private static Map<QName, Method> getYangLeafListGetters(Class klass, QName klassQName) {
        Map<QName, Method> getters = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            YangLeafList yangLeafListAnnotation = field.getAnnotation(YangLeafList.class);

            if (yangLeafListAnnotation != null) {
                addGetter(klass, klassQName, getters, field);
            }
        }
        return getters;
    }

    private static Map<QName, Method> getYangLeafListSetters(Class klass, QName klassQName) {
        Map<QName, Method> setters = new HashMap<>();
        for (Field field : klass.getDeclaredFields()) {
            YangLeafList yangLeafListAnnotation = field.getAnnotation(YangLeafList.class);

            if (yangLeafListAnnotation != null) {
                addSetter(klass, klassQName, setters, field);
            }
        }
        return setters;
    }

    private static void addGetter(Class klass, QName klassQName, Map<QName, Method> getters, Field field) {
        Method getter = null;
        try {
            getter = AnnotationUtil.getGetMethod(klass, field);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        QName attrQname = getQName(field.getName(), field.getDeclaredAnnotations(), klassQName);
        getters.put(attrQname, getter);
    }

    private static void addSetter(Class klass, QName klassQName, Map<QName, Method> setters, Field field) {
        Method setter = null;
        try {
            setter = AnnotationUtil.getSetMethod(klass, field);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        QName attrQname = getQName(field.getName(), field.getDeclaredAnnotations(), klassQName);
        setters.put(attrQname, setter);
    }

    private static void addField(QName klassQName, Map<QName, String> fieldNames, Field field) {
        QName attrQname = getQName(field.getName(), field.getDeclaredAnnotations(), klassQName);
        fieldNames.put(attrQname, field.getName());
    }

    private static void checkQNameComponents(Annotation[] annotations, Map<String, String> components) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof YangContainer) {
                components.put(NAME, ((YangContainer) annotation).name());
                components.put(NAMESPACE, ((YangContainer) annotation).namespace());
                components.put(REVISION, ((YangContainer) annotation).revision());
                return;
            }
            if (annotation instanceof YangChild) {
                components.put(NAME, ((YangChild) annotation).name());
                components.put(NAMESPACE, ((YangChild) annotation).namespace());
                components.put(REVISION, ((YangChild) annotation).revision());
                return;
            }
            if (annotation instanceof YangAttribute) {
                components.put(NAME, ((YangAttribute) annotation).name());
                components.put(NAMESPACE, ((YangAttribute) annotation).namespace());
                components.put(REVISION, ((YangAttribute) annotation).revision());
                return;
            }
            if (annotation instanceof YangList) {
                components.put(NAME, ((YangList) annotation).name());
                components.put(NAMESPACE, ((YangList) annotation).namespace());
                components.put(REVISION, ((YangList) annotation).revision());
                return;
            }
            if (annotation instanceof YangListKey) {
                components.put(NAME, ((YangListKey) annotation).name());
                components.put(NAMESPACE, ((YangListKey) annotation).namespace());
                components.put(REVISION, ((YangListKey) annotation).revision());
                return;
            }
            if (annotation instanceof YangLeafList) {
                components.put(NAME, ((YangLeafList) annotation).name());
                components.put(NAMESPACE, ((YangLeafList) annotation).namespace());
                components.put(REVISION, ((YangLeafList) annotation).revision());
                return;
            }
            if (annotation instanceof YangXmlSubtree) {
                components.put(NAME, ((YangXmlSubtree) annotation).name());
                components.put(NAMESPACE, ((YangXmlSubtree) annotation).namespace());
                components.put(REVISION, ((YangXmlSubtree) annotation).revision());
                return;
            }
        }
    }

    public static QName getQName(Class klass, QName parentQname) {
        String simpleName = klass.getSimpleName();
        return getQName(simpleName, klass.getDeclaredAnnotations(), parentQname);
    }

    public static QName getQName(String simpleName, Annotation[] annotations, QName parentQname) {
        Map<String, String> components = new HashMap<>();
        checkQNameComponents(annotations, components);

        String name = components.get(NAME);
        String namespace = components.get(NAMESPACE);
        String revision = components.get(REVISION);


        if (name == null || name.isEmpty()) {
            name = simpleName;
        }
        if (namespace == null || namespace.isEmpty()) {
            if (parentQname != null) {
                namespace = parentQname.getNamespace().toString();
            }
        }
        if (revision == null || revision.isEmpty()) {
            if (parentQname != null) {
                revision = parentQname.getFormattedRevision();
            }
        }
        if (revision != null && !revision.isEmpty()) {
            return QName.create(namespace, revision, name);
        }
        return QName.create(namespace, name);
    }

    private static List<Class> getRootClasses(List<Class> entityClasses) {
        List<Class> copy = new ArrayList<>(entityClasses);
        Iterator<Class> classIter = entityClasses.iterator();

        //assume all classes are roots, then start refining
        while (classIter.hasNext()) {
            Class klass = classIter.next();
            for (Field field : klass.getDeclaredFields()) {
                Type effectiveType = field.getType();
                if (field.getGenericType() instanceof ParameterizedType) {
                    ParameterizedType type = (ParameterizedType) field.getGenericType();
                    //only lists and sets as of now
                    effectiveType = type.getActualTypeArguments()[0];

                }
                if (copy.contains(effectiveType)) {
                    copy.remove(effectiveType);
                }
            }
        }
        return copy;
    }
}
