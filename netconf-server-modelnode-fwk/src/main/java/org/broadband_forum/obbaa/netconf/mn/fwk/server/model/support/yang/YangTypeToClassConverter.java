
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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import java.math.BigInteger;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Utility methods and constants to work with built-in YANG types
 */
public final class YangTypeToClassConverter {


    public static Class<?> getClass(final TypeDefinition<?> type) {
        if (type != null) {
            if (type instanceof UnionTypeDefinition) {
                return getClass(((UnionTypeDefinition) type).getTypes().get(0));
            }
            if (DerivedTypes.isInt8(type)) {
                return Byte.class;
            } else if (DerivedTypes.isInt16(type)) {
                return Short.class;
            } else if (DerivedTypes.isInt32(type)) {
                return Integer.class;
            } else if (DerivedTypes.isInt64(type)) {
                return Long.class;
            } else if (DerivedTypes.isUint8(type)) {
                return Short.class;
            } else if (DerivedTypes.isUint16(type)) {
                return Integer.class;
            } else if (DerivedTypes.isUint32(type)) {
                return Long.class;
            } else if (DerivedTypes.isUint64(type)) {
                return BigInteger.class;
            } else if (type instanceof StringTypeDefinition) {
                return String.class;
            } else if (type instanceof DecimalTypeDefinition) {
                return Double.class;
            } else if (type instanceof BinaryTypeDefinition) {
                return Byte[].class;
            } else if (type instanceof BooleanTypeDefinition) {
                return Boolean.class;
            } else if (type instanceof EmptyTypeDefinition) {
                return String.class;
            } else if (type instanceof InstanceIdentifierTypeDefinition) {
                return InstanceIdentifierTypeDefinition.class;
            } else if (type instanceof EnumTypeDefinition) {
                return String.class;
            } else if (type instanceof IdentityrefTypeDefinition || type instanceof IdentityTypeDefinition) {
                return QName.class;
            }
        }
        return null;
    }

    /**
     * Provides java classType of specified leaf or leaf-list schemaPath.
     *
     * @param schemaRegistry
     * @param schemaPath
     * @return
     */
    public static Class<?> getClassTypeBySchemaPath(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        DataSchemaNode dataSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        TypeDefinition<?> type = null;
        if (dataSchemaNode instanceof LeafSchemaNode) {
            type = ((LeafSchemaNode) dataSchemaNode).getType();
        } else if (dataSchemaNode instanceof LeafListSchemaNode) {
            type = ((LeafListSchemaNode) dataSchemaNode).getType();
        }
        Class<?> classType = YangTypeToClassConverter.getClass(type);
        return classType;
    }


}
