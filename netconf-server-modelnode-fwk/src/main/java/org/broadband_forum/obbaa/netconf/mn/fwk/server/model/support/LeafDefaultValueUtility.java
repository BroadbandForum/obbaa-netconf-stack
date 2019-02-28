package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.math.BigInteger;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangTypeToClassConverter;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

import com.google.common.annotations.VisibleForTesting;

public class LeafDefaultValueUtility {
    
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(LeafDefaultValueUtility.class, LogAppNames.NETCONF_STACK);
    
    public static String getDefaultValue(LeafSchemaNode leafSchemaNode)  {
        String defaultValue = getSchemaDefaultValue(leafSchemaNode);
        if (defaultValue != null) {
            LOGGER.debug("Schema default value is {}",defaultValue);
            TypeDefinition<?> yangType = leafSchemaNode.getType();
            String transformedDefaultValue = getDefaultValueForYangType(leafSchemaNode, defaultValue, yangType);
            if (transformedDefaultValue != null) {
                return transformedDefaultValue;
            }
        }
        return defaultValue;
    }

    private static String getDefaultValueForYangType(LeafSchemaNode leafSchemaNode, String defaultValue, TypeDefinition<?> yangType) {
        if (yangType instanceof UnionTypeDefinition) {
            String transformedDefaultValue = null;
            for (TypeDefinition<?> memberType : ((UnionTypeDefinition) yangType).getTypes()) {
                try {
                    Class<?> javaType = YangTypeToClassConverter.getClass(memberType);
                    transformedDefaultValue = StringToObjectTransformer.transform(defaultValue, javaType).toString();
                } catch (TransformerException e) {
                    // should be ignored here, unless no member type matches
                }
            }
            if (transformedDefaultValue == null) {
                LOGGER.warn("could not transform default value " + defaultValue + " for LeafSchemaNode " + leafSchemaNode);
            }
            return null;
        }
        else {
            try {
                Class<?> javaType = YangTypeToClassConverter.getClass(yangType);
                return StringToObjectTransformer.transform(defaultValue, javaType).toString();
            } catch (TransformerException e) {
                LOGGER.warn("could not transform default value " + defaultValue + " for LeafSchemaNode " + leafSchemaNode, e);
                return null;
            }
        }
    }

    private static String getSchemaDefaultValue(LeafSchemaNode leafSchemaNode) {
        String defaultValue = null;
        TypeDefinition<?> type = leafSchemaNode.getType();
        if(type instanceof EmptyTypeDefinition){
            defaultValue = "";
        }else{
            Optional<? extends Object> optDefaultValue = type.getDefaultValue();
            if (optDefaultValue.isPresent()) {
                Object defaultVal = optDefaultValue.get();
                defaultValue = defaultVal.toString();
            }

        }
        if(defaultValue != null && isIntegerBasedType(type)) {
            defaultValue = transformToDecimal(type, defaultValue);
        }
        return defaultValue;
    }

    private static String transformToDecimal(TypeDefinition<?> type, String defaultValue) {
        boolean negative = false;
        if(defaultValue.startsWith("+")) {
            defaultValue = defaultValue.substring(1);
        } else if(defaultValue.startsWith("-")) {
            defaultValue = defaultValue.substring(1);
            negative = true;
        }

        if(defaultValue.startsWith("0x") || defaultValue.startsWith("0X")) {
            defaultValue = transformHexToDecimal(type, defaultValue.substring(2), negative);
        } else if(defaultValue.startsWith("0") && defaultValue.length() > 1) {
            defaultValue = transformOctalToDecimal(type, defaultValue.substring(1), negative);
        } else if(isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }
        
        return defaultValue;
    }

    private static String transformHexToDecimal(TypeDefinition<?> type, String defaultValue, boolean negative) {
        if(isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }
        
        if (type instanceof Int8TypeDefinition) {
            return ((Byte)Byte.parseByte(defaultValue, 16)).toString();
        } else if (type instanceof Int16TypeDefinition) {
            return ((Short)Short.parseShort(defaultValue, 16)).toString();
        } else if (type instanceof Int32TypeDefinition) {
            return ((Integer)Integer.parseInt(defaultValue, 16)).toString();
        } else if (type instanceof Int64TypeDefinition) {
            return((Long)Long.parseLong(defaultValue, 16)).toString();
        } else if (type instanceof Uint8TypeDefinition) {
            return ((Short)Short.parseShort(defaultValue, 16)).toString();
        } else if (type instanceof Uint16TypeDefinition) {
            return ((Integer)Integer.parseInt(defaultValue, 16)).toString();
        } else if (type instanceof Uint32TypeDefinition) {
            return((Long)Long.parseLong(defaultValue, 16)).toString();
        } else if (type instanceof Uint64TypeDefinition) {
            return new BigInteger(defaultValue, 16).toString();
        }
        return defaultValue;
    }

    private static String transformOctalToDecimal(TypeDefinition<?> type, String defaultValue, boolean negative) {
        if(isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }
        
        if (type instanceof Int8TypeDefinition) {
            return ((Byte)Byte.parseByte(defaultValue, 8)).toString();
        } else if (type instanceof Int16TypeDefinition) {
            return ((Short)Short.parseShort(defaultValue, 8)).toString();
        } else if (type instanceof Int32TypeDefinition) {
            return ((Integer)Integer.parseInt(defaultValue, 8)).toString();
        } else if (type instanceof Int64TypeDefinition) {
            return((Long)Long.parseLong(defaultValue, 8)).toString();
        } else if (type instanceof Uint8TypeDefinition) {
            return ((Short)Short.parseShort(defaultValue, 8)).toString();
        } else if (type instanceof Uint16TypeDefinition) {
            return ((Integer)Integer.parseInt(defaultValue, 8)).toString();
        } else if (type instanceof Uint32TypeDefinition) {
            return((Long)Long.parseLong(defaultValue, 8)).toString();
        } else if (type instanceof Uint64TypeDefinition) {
            return new BigInteger(defaultValue, 8).toString();
        }
        return defaultValue;
    }

    private static boolean isIntegerBasedType(TypeDefinition<?> type) {
        return type instanceof Int8TypeDefinition
                || type instanceof Int16TypeDefinition
                || type instanceof Int32TypeDefinition
                || type instanceof Int64TypeDefinition
                || type instanceof Uint8TypeDefinition
                || type instanceof Uint16TypeDefinition
                || type instanceof Uint32TypeDefinition
                || type instanceof Uint64TypeDefinition;
    }
    
    private static boolean isSigned(TypeDefinition<?> type) {
        return type instanceof Int8TypeDefinition
                || type instanceof Int16TypeDefinition
                || type instanceof Int32TypeDefinition
                || type instanceof Int64TypeDefinition;
    }
    
    @VisibleForTesting
    static AdvancedLogger getLoggerForTest() {
        return LOGGER;
    }
}
