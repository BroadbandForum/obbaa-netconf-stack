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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StringToObjectTransformer.transform;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isInt16;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isInt32;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isInt64;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isInt8;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isUint16;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isUint32;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isUint64;
import static org.opendaylight.yangtools.yang.model.util.type.DerivedTypes.isUint8;

import java.math.BigInteger;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangTypeToClassConverter;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class LeafDefaultValueUtility {

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(LeafDefaultValueUtility.class,
            "netconf-stack", "DEBUG", "GLOBAL");

    public static String getDefaultValue(LeafSchemaNode leafSchemaNode) {
        Class<?> javaType = YangTypeToClassConverter.getClass(leafSchemaNode.getType());
        String defaultValue = getSchemaDefaultValue(leafSchemaNode);
        if (defaultValue != null) {
            try {
                LOGGER.debug("Schema default value is {}", defaultValue);
                return StringToObjectTransformer.transform(defaultValue, javaType).toString();
            } catch (TransformerException e) {
                LOGGER.warn("could not transform to LeafSchemaNode default value", e);
            }
        }
        return defaultValue;
    }

    private static String getSchemaDefaultValue(LeafSchemaNode leafSchemaNode) {
        String defaultValue = leafSchemaNode.getDefault();
        TypeDefinition<?> type = leafSchemaNode.getType();
        if (defaultValue == null) {
            Object defaultVal = type.getDefaultValue();
            if (type instanceof EmptyTypeDefinition) {
                defaultValue = "";
            } else {
                defaultValue = defaultVal.toString();
            }

        }
        if (isIntegerBasedType(type)) {
            defaultValue = transformToDecimal(type, defaultValue);
        }
        return defaultValue;
    }

    private static String transformToDecimal(TypeDefinition<?> type, String defaultValue) {
        boolean negative = false;
        if (defaultValue.startsWith("+")) {
            defaultValue = defaultValue.substring(1);
        } else if (defaultValue.startsWith("-")) {
            defaultValue = defaultValue.substring(1);
            negative = true;
        }

        if (defaultValue.startsWith("0x") || defaultValue.startsWith("0X")) {
            defaultValue = transformHexToDecimal(type, defaultValue.substring(2), negative);
        } else if (defaultValue.startsWith("0") && defaultValue.length() > 1) {
            defaultValue = transformOctalToDecimal(type, defaultValue.substring(1), negative);
        } else if (isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }

        return defaultValue;
    }

    private static String transformHexToDecimal(TypeDefinition<?> type, String defaultValue, boolean negative) {
        if (isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }

        if (DerivedTypes.isInt8(type)) {
            return ((Byte) Byte.parseByte(defaultValue, 16)).toString();
        } else if (DerivedTypes.isInt16(type)) {
            return ((Short) Short.parseShort(defaultValue, 16)).toString();
        } else if (DerivedTypes.isInt32(type)) {
            return ((Integer) Integer.parseInt(defaultValue, 16)).toString();
        } else if (DerivedTypes.isInt64(type)) {
            return ((Long) Long.parseLong(defaultValue, 16)).toString();
        } else if (DerivedTypes.isUint8(type)) {
            return ((Short) Short.parseShort(defaultValue, 16)).toString();
        } else if (DerivedTypes.isUint16(type)) {
            return ((Integer) Integer.parseInt(defaultValue, 16)).toString();
        } else if (DerivedTypes.isUint32(type)) {
            return ((Long) Long.parseLong(defaultValue, 16)).toString();
        } else if (DerivedTypes.isUint64(type)) {
            return new BigInteger(defaultValue, 16).toString();
        }
        return defaultValue;
    }

    private static String transformOctalToDecimal(TypeDefinition<?> type, String defaultValue, boolean negative) {
        if (isSigned(type) && negative) {
            defaultValue = "-" + defaultValue;
        }

        if (DerivedTypes.isInt8(type)) {
            return ((Byte) Byte.parseByte(defaultValue, 8)).toString();
        } else if (DerivedTypes.isInt16(type)) {
            return ((Short) Short.parseShort(defaultValue, 8)).toString();
        } else if (DerivedTypes.isInt32(type)) {
            return ((Integer) Integer.parseInt(defaultValue, 8)).toString();
        } else if (DerivedTypes.isInt64(type)) {
            return ((Long) Long.parseLong(defaultValue, 8)).toString();
        } else if (DerivedTypes.isUint8(type)) {
            return ((Short) Short.parseShort(defaultValue, 8)).toString();
        } else if (DerivedTypes.isUint16(type)) {
            return ((Integer) Integer.parseInt(defaultValue, 8)).toString();
        } else if (DerivedTypes.isUint32(type)) {
            return ((Long) Long.parseLong(defaultValue, 8)).toString();
        } else if (DerivedTypes.isUint64(type)) {
            return new BigInteger(defaultValue, 8).toString();
        }
        return defaultValue;
    }

    private static boolean isIntegerBasedType(TypeDefinition<?> type) {
        return isInt8(type) || isInt16(type) || isInt32(type) || isInt64(type) || isUint8(type) || isUint16(type) ||
                isUint32(type)
                || isUint64(type);
    }

    private static boolean isSigned(TypeDefinition<?> type) {
        return isInt8(type) || isInt16(type) || isInt32(type) || isInt64(type);
    }
}
