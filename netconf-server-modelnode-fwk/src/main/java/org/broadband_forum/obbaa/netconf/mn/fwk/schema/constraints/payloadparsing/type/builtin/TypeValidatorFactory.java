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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BinaryTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BooleanTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IntegerTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class TypeValidatorFactory {

    private static final TypeValidatorFactory INSTANCE = new TypeValidatorFactory();
    private Map<TypeDefinition<?>, TypeValidator> m_validators = new HashMap<TypeDefinition<?>, TypeValidator>();
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(TypeValidatorFactory.class, "netconf-stack",
            "DEBUG", "GLOBAL");


    private TypeValidatorFactory() {
    }


    public static TypeValidatorFactory getInstance() {
        return INSTANCE;
    }

    public TypeValidator getValidator(TypeDefinition<?> type) {
        TypeValidator typeValidator = m_validators.get(type);

        if (typeValidator == null) {
            if (type instanceof StringTypeDefinition) {
                String localTypeName = type.getQName().getLocalName();
                if (!URLTypeConstraintParser.LOCAL_TYPE_NAME.equals(localTypeName)) {
                    if (DateAndTimeTypeConstraintParser.LOCAL_TYPE_NAME.equals(localTypeName)) {
                        typeValidator = new DateAndTimeTypeConstraintParser(type);
                    } else {
                        typeValidator = new StringTypeConstraintParser(type);
                    }
                }
            } else if (type instanceof UnsignedIntegerTypeDefinition) {
                typeValidator = new UnsignedIntegerTypeConstraintParser(type);
            } else if (type instanceof IntegerTypeDefinition) {
                typeValidator = new IntegerTypeConstraintParser(type);
            } else if (type instanceof DecimalTypeDefinition) {
                typeValidator = new DecimalTypeConstraintParser(type);
            } else if (type instanceof BinaryTypeDefinition) {
                typeValidator = new BinaryTypeConstraintParser(type);
            } else if (type instanceof EnumTypeDefinition) {
                typeValidator = new EnumerationTypeConstraintParser(type);
            } else if (type instanceof UnionTypeDefinition) {
                typeValidator = new UnionTypeConstraintParser((UnionTypeDefinition) type);
            } else if (type instanceof IdentityrefTypeDefinition) {
                typeValidator = new IdentityRefTypeConstraintParser(type);
            } else if (type instanceof EmptyTypeDefinition) {
                typeValidator = new EmptyTypeConstraintParser(type);
            } else if (type instanceof BitsTypeDefinition) {
                typeValidator = new BitsTypeConstraintParser(type);
            } else if (type instanceof BooleanTypeDefinition) {
                typeValidator = new BooleanTypeConstraintParser();
            } else if (type instanceof LeafrefTypeDefinition) {
                // nothing to do during payload parsing
            } else {
                LOGGER.warn(type + " is unknown and not validated");
            }

            if (typeValidator != null) {
                m_validators.put(type, typeValidator);
            }
        }

        return typeValidator;
    }
}
