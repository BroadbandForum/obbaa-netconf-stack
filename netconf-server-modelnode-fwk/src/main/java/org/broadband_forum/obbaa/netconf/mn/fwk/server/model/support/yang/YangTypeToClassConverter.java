
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
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

/**
 * Utility methods and constants to work with built-in YANG types
 *
 *
 */
public final class YangTypeToClassConverter {


    public static Class<?> getClass(final TypeDefinition<?> type) {
    	if (type != null){
        	if(type instanceof UnionTypeDefinition){
        		return getClass(((UnionTypeDefinition)type).getTypes().get(0));
        	}
            if (type instanceof Int8TypeDefinition) {
                return Byte.class;
            } else if (type instanceof Int16TypeDefinition) {
                return Short.class;
            } else if (type instanceof Int32TypeDefinition) {
                return Integer.class;
            } else if (type instanceof Int64TypeDefinition) {
                return Long.class;
            } else if (type instanceof Uint8TypeDefinition) {
                return Short.class;
            } else if (type instanceof Uint16TypeDefinition) {
                return Integer.class;
            } else if (type instanceof Uint32TypeDefinition) {
                return Long.class;
            } else if (type instanceof Uint64TypeDefinition) {
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
            } else if(type instanceof IdentityrefTypeDefinition || type instanceof IdentityTypeDefinition) {
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
        if(dataSchemaNode instanceof LeafSchemaNode) {
            type =((LeafSchemaNode) dataSchemaNode).getType();
        } else if(dataSchemaNode instanceof LeafListSchemaNode) {
            type =((LeafListSchemaNode) dataSchemaNode).getType();
        }
        Class<?> classType = YangTypeToClassConverter.getClass(type);
        return classType;
    }


}
