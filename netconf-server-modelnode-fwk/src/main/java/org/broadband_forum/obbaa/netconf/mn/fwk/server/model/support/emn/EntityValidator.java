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

import static org.broadband_forum.obbaa.netconf.api.util.ReflectionUtils.getAllDeclaredFields;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * EntityValidator iterates through the entity classes and does the following checks using schemaRegistry
 * a. If there is a YangChild/YangAttribute/YangList, check if there is actually a yang child/yang attribute/yang list for the given node
 * b. Each entity class has YangSchemaPath and YangParentId.
 */
public class EntityValidator {

    public static final String NO_YANG_SCHEMA_PATH = "No YangSchemaPath defined for class - ";
    public static final String NO_YANG_PARENT_ID = "No YangParentId defined for class - ";
    public static final String INVALID_YANG_CHILD = "Invalid Yang child found - ";
    public static final String INVALID_YANG_CHILD_MUST_VE_BEEN_A_LIST = "Invalid Yang child found, must've been a list - ";
    public static final String INVALID_ATTRIBUTE = "Invalid attribute found OR name component not specified- ";

    public static Map<Class,List<String>> validateRootClasses(SchemaRegistry schemaRegistry, List<Class> rootClasses){
        Map<Class,List<String>> errors = new HashMap<>();
        for(Class rootClass: rootClasses){
            SchemaPath rootSchemaPath = getRootSchemaPath(schemaRegistry, rootClass);
            if (rootSchemaPath!=null) {
                validateAnnotations(schemaRegistry, rootSchemaPath, rootClass, errors);
            }else{
                addError(rootClass,errors,"Root SchemaPath missing from SchemaRegistry");
            }
        }
        return errors;
    }

    private static void validateAnnotations(SchemaRegistry schemaRegistry, SchemaPath parentSchemaPath, Class klass, Map<Class, List<String>> errors) {
        List<Field> fields = getAllDeclaredFields(klass);
        boolean parentIdAnnotationExists = false;
        boolean schemaPathAnnotationExists = false;

        for(Field field:fields){
            Annotation[] fieldAnnotations = field.getAnnotations();
            for(Annotation annotation: fieldAnnotations){

                if(annotation instanceof YangSchemaPath){
                    schemaPathAnnotationExists = true;
                }else if(annotation instanceof YangParentId){
                    parentIdAnnotationExists = true;
                }else if(annotation instanceof YangAttribute || annotation instanceof YangListKey){
                    validateYangAttribute(schemaRegistry, parentSchemaPath, field, klass, errors);
                }else if(annotation instanceof YangChild){
                    validateYangChild(schemaRegistry, parentSchemaPath, field, klass, errors);
                }
            }
        }

        if(!parentIdAnnotationExists){
            String error = NO_YANG_SCHEMA_PATH + klass.getName();
            addError(klass,errors,error);
        }
        if(!schemaPathAnnotationExists){
            String error = NO_YANG_PARENT_ID + klass.getName();
            addError(klass,errors,error);
        }
    }

    private static void validateYangChild(SchemaRegistry schemaRegistry, SchemaPath schemaPath, Field field, Class klass, Map<Class, List<String>> errors) {
        boolean isChildAList = false;
        Type type = field.getType();
        Collection<DataSchemaNode> dataSchemaNodes = schemaRegistry.getChildren(schemaPath);

        if(field.getGenericType() instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
            type = parameterizedType.getActualTypeArguments()[0];
            isChildAList = true;
        }
        Class aClass = (Class) type;
        QName qName = getQName(aClass, aClass.getDeclaredAnnotations(), schemaPath);
        boolean found = false;
        String error = INVALID_YANG_CHILD + aClass.getName();
        for(DataSchemaNode dataSchemaNode : dataSchemaNodes){
            if(dataSchemaNode instanceof ContainerSchemaNode){
                if(dataSchemaNode.getQName().equals(qName)){
                    found = true;
                    break;
                }
            }else if(dataSchemaNode instanceof  ListSchemaNode){
                if(dataSchemaNode.getQName().equals(qName)){
                    if(isChildAList){
                        found = true;
                        break;
                    }else{
                        error = INVALID_YANG_CHILD_MUST_VE_BEEN_A_LIST + aClass.getName();
                        break;
                    }
                }
            }
        }
        if(!found){
            addError(klass,errors,error);
        }else {
            SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(schemaPath, qName);
            validateAnnotations(schemaRegistry, childSchemaPath, aClass, errors);
        }
    }

    private static void validateYangAttribute(SchemaRegistry schemaRegistry, SchemaPath parentSchemaPath, Field field, Class klass, Map<Class, List<String>> errors) {
        QName qName = getQName(klass, field.getDeclaredAnnotations(), parentSchemaPath);
        boolean found = false;
        Collection<DataSchemaNode> children = schemaRegistry.getChildren(parentSchemaPath);
        for(DataSchemaNode dataSchemaNode : children){
            if(dataSchemaNode instanceof LeafSchemaNode){
                if(dataSchemaNode.getPath().getLastComponent().equals(qName)){
                    found = true;
                    break;
                }
            }
        }
        if(!found){
            String error = INVALID_ATTRIBUTE + field.getName();
            addError(klass,errors,error);
        }
    }

    private static SchemaPath getRootSchemaPath(SchemaRegistry schemaRegistry, Class aClass) {
        Iterator<DataSchemaNode> schemaNodeIterator = schemaRegistry.getRootDataSchemaNodes().iterator();
        QName qname = EntityRegistryBuilder.getQName(aClass, null);
        while (schemaNodeIterator.hasNext()){
            DataSchemaNode dataSchemaNode = schemaNodeIterator.next();
            if(dataSchemaNode.getPath().getLastComponent().equals(qname)){
                return dataSchemaNode.getPath();
            }
        }
        return null;
    }

    private static QName getQName(Class klass, Annotation[] declaredAnnotations, SchemaPath parentSchemaPath) {
        QName qName = EntityRegistryBuilder.getQName(klass.getName(), declaredAnnotations, parentSchemaPath.getLastComponent());
        return qName;
    }

    private static void addError(Class aClass, Map<Class, List<String>> errors, String error) {
        List<String> errorsOfClass = errors.get(aClass);
        if(errorsOfClass==null){
            errorsOfClass = new ArrayList<>();
            errors.put(aClass,errorsOfClass);
        }
        errorsOfClass.add(error);
    }
}
