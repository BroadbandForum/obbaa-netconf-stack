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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin.BooleanTypeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.BitsTypeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.IdentityRefUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
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
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.w3c.dom.Node;


public class TransformerUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(TransformerUtil.class, LogAppNames.NETCONF_STACK);

    public static Object convert(TypeDefinition<?> type, String value) throws TransformerException {
        if (type instanceof UnionTypeDefinition) {
            TypeDefinition<?> childTypeDefinition = findUnionType(((UnionTypeDefinition) type).getTypes(), value);
            return convert(childTypeDefinition, value);
        } else if (type instanceof EnumTypeDefinition) {
            for (EnumTypeDefinition.EnumPair enumPair : ((EnumTypeDefinition) type).getValues()) {
                if (enumPair.getName().equals(value)) {
                    return enumPair.getName();
                }
            }
            throw new TransformerException("Invalid enum value");
        } else {
            Class<?> javaType = getClass(type);
            return transform(value, javaType);
        }
    }

    public static TypeDefinition<?> findUnionType(List<TypeDefinition<?>> typeDefinitions, Node node, String value) {
        for (TypeDefinition td : typeDefinitions) {
            if (td instanceof UnionTypeDefinition) {
                TypeDefinition<?> matchingType = findUnionType(((UnionTypeDefinition) td).getTypes(), node, value);
                if ( matchingType != null ) return matchingType;
            } else if (td instanceof IdentityrefTypeDefinition) {
                String stringValue = value;
                String namespace;
                String identityValue;

                try {
                    namespace = IdentityRefUtil.getNamespace(node, stringValue);
                    identityValue = IdentityRefUtil.getIdentityValue(stringValue);
                } catch (Exception ex) {
                    continue;
                }
                boolean isValid;
                for (IdentitySchemaNode identitySchemaNode : obtainDerivedIdentities(td)) {
                    isValid = IdentityRefUtil.identityMatches(identitySchemaNode, namespace, identityValue);
                    if (!isValid) {
                        isValid = IdentityRefUtil.checkDerivedIdentities(identitySchemaNode, namespace, identityValue);
                    }
                    if (isValid) {
                        return td;
                    }
                }
            } else if (td instanceof InstanceIdentifierTypeDefinition) {
                if (checkIfInstanceIdentifier(value)) {
                    return td;
                }
            } else if (td instanceof EnumTypeDefinition) {
                for (EnumTypeDefinition.EnumPair enumPair : ((EnumTypeDefinition) td).getValues()) {
                    if (enumPair.getName().equals(value)) {
                        return td;
                    }
                }
            } else if (td instanceof BooleanTypeDefinition) {
                String textContent = value;
                if (BooleanTypeConstraintParser.TRUE.equals(textContent) || BooleanTypeConstraintParser.FALSE.equals(textContent)) {
                    return td;
                }
            } else if (td instanceof BitsTypeDefinition) {
                if (BitsTypeUtil.isBitsType((BitsTypeDefinition) td, value)){
                    return td;
                }

            } else {
                try {
                    if ( td instanceof  StringTypeDefinition){
                        List<PatternConstraint> patternConstraints = ((StringTypeDefinition) td).getPatternConstraints();
                        if (! patternConstraints.isEmpty()){
                            boolean isMatching = false;
                            for ( PatternConstraint patternConstraint : patternConstraints){
                                String javaPatternString = patternConstraint.getJavaPatternString();
                                isMatching = value.matches(javaPatternString);
                                if (isMatching) break;
                            }
                            if (!isMatching){
                                throw new TransformerException("Pattern not matching for string type");
                            }
                        }
                    }
                    Class<?> javaType = getClass(td);
                    transform(value, javaType);
                    return td;
                } catch (TransformerException e) {
                    // should be ignored here, unless no member type matches
                }
            }
        }
        return null;
    }

    private static TypeDefinition<?> findUnionType(List<TypeDefinition<?>> typeDefinitions, String value) {
        for (TypeDefinition td : typeDefinitions) {
            if (td instanceof UnionTypeDefinition) {
                return findUnionType(((UnionTypeDefinition) td).getTypes(), value);
            } else if (td instanceof InstanceIdentifierTypeDefinition) {
                if (checkIfInstanceIdentifier(value)) {
                    return td;
                }
            } else if (td instanceof EnumTypeDefinition) {
                for (EnumTypeDefinition.EnumPair enumPair : ((EnumTypeDefinition) td).getValues()) {
                    if (enumPair.getName().equals(value)) {
                        return td;
                    }
                }
            } else {
                try {
                    Class<?> javaType = getClass(td);
                    transform(value, javaType);
                    return td;
                } catch (TransformerException e) {
                    // should be ignored here, unless no member type matches
                }
            }
        }
        return null;
    }

    private static Set<IdentitySchemaNode> obtainDerivedIdentities(TypeDefinition type) {
        IdentityrefTypeDefinition baseType;
        Set<IdentitySchemaNode> derivedIdentities = new HashSet<>();
        baseType = (IdentityrefTypeDefinition) type;
        Set<IdentitySchemaNode> identitySchemaNodes = baseType.getIdentities();
        for (IdentitySchemaNode identitySchemaNode : identitySchemaNodes) {
            derivedIdentities.addAll(identitySchemaNode.getDerivedIdentities());
        }
        return derivedIdentities;
    }

    private static Class<?> getClass(final TypeDefinition<?> type) {
        if (type != null) {
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
            } else if (type instanceof EnumTypeDefinition) {
                return Enum.class;
            } else if (type instanceof InstanceIdentifierTypeDefinition) {
                return InstanceIdentifierTypeDefinition.class;
            } else if (type instanceof IdentityrefTypeDefinition) {
                return IdentityrefTypeDefinition.class;
            } else if (type instanceof IdentityTypeDefinition) {
                return IdentityTypeDefinition.class;
            }
        }
        return null;
    }

    private static boolean checkIfInstanceIdentifier(String xpath) {
        try {
            JXPathContext.compile(xpath);
            return true;
        } catch (Exception ex) {
            LOGGER.info("The instance identifier is not a valid Xpath");
            return false;
        }
    }

    public static Object transform(String strValue, Class<?> class1) throws TransformerException {
        if (class1 == null) {
            // We do not have a specific type parser. We do not validate.
            return strValue;
        }
        if (class1.equals(Integer.class) || class1.equals(Integer.TYPE)) {
            return transformToInteger(strValue);
        }
        if (class1.equals(Long.class) || class1.equals(Long.TYPE)) {
            return transformToLong(strValue);
        }
        if (class1.equals(String.class) || class1.equals(Byte[].class) || class1.equals(Double.class)) {
            return strValue;
        }
        if (class1.equals(Short.class) || class1.equals(Short.TYPE)) {
            return transformToShort(strValue);
        }
        if (class1.equals(BigInteger.class)) {
            return transformToBigInteger(strValue);
        }
        if (class1.equals(Byte.class) || class1.equals(Byte.TYPE)) {
            return transformToByte(strValue);
        }
        if (class1.equals(Boolean.class) || class1.equals(Boolean.TYPE)) {
            return Boolean.valueOf(strValue);
        }
        if (class1.equals(IdentityrefTypeDefinition.class) || class1.equals(IdentityTypeDefinition.class) || class1.equals(InstanceIdentifierTypeDefinition.class)) {
            return strValue;
        }
        throw new TransformerException("No transformer found for : " + class1);
    }

    private static BigInteger transformToBigInteger(String strValue) throws TransformerException {
        try {
            return new BigInteger(strValue);
        } catch (NumberFormatException e) {
            NetconfRpcError rpcError = buildNetconfRpcError("Invalid value : " + strValue + ", it should be a BigInteger");
            throw new TransformerException(rpcError);
        }
    }

    private static Byte transformToByte(String strValue) throws TransformerException {
        try {
            return Byte.valueOf(strValue);
        } catch (NumberFormatException e) {
            NetconfRpcError rpcError =
                    buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Byte with range from " + Byte.MIN_VALUE + " to" +
                            " " + Byte.MAX_VALUE);
            throw new TransformerException(rpcError);
        }
    }

    private static Short transformToShort(String strValue) throws TransformerException {
        try {
            return Short.valueOf(strValue);
        } catch (NumberFormatException e) {
            NetconfRpcError rpcError =
                    buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Short with range from " + Short.MIN_VALUE + " " +
                            "to " + Short.MAX_VALUE);
            throw new TransformerException(rpcError);
        }
    }

    private static Long transformToLong(String strValue) throws TransformerException {
        try {
            return Long.valueOf(strValue);
        } catch (NumberFormatException e) {
            NetconfRpcError rpcError =
                    buildNetconfRpcError("Invalid value : " + strValue + ", it should be a Long with range from " + Long.MIN_VALUE + " to" +
                            " " + Long.MAX_VALUE);
            throw new TransformerException(rpcError);
        }
    }

    private static Integer transformToInteger(String strValue) throws TransformerException {
        try {
            return Integer.valueOf(strValue);
        } catch (NumberFormatException e) {
            NetconfRpcError rpcError =
                    buildNetconfRpcError("Invalid value : " + strValue + ", it should be an Integer with range from " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);
            throw new TransformerException(rpcError);
        }
    }

    private static NetconfRpcError buildNetconfRpcError(String message) {
        NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.INVALID_VALUE,
                message);
        return rpcError;
    }
}
