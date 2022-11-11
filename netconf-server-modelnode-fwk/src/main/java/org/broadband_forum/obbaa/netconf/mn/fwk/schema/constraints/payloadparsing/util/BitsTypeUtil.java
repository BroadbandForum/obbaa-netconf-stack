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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

import java.util.List;

public class BitsTypeUtil {

    public static String orderBitsValue(LeafSchemaNode leafSchemaNode, String bitsValue) {
        TypeDefinition<? extends TypeDefinition<?>> type = leafSchemaNode.getType();
        return orderBitsValue(type, bitsValue);
    }

    public static boolean isBitsType(BitsTypeDefinition type, String bitsValue) {
        List<BitsTypeDefinition.Bit> bits = type.getBits();
        String[] split = StringUtils.split(bitsValue);
        boolean found = false;
        for (String splitBit : split) {
            for (BitsTypeDefinition.Bit bit : bits) {
                if (bit.getName().equals(splitBit)){
                    found = true;
                    break;
                }
            }
            if( ! found){
                return false;
            }
        }
        return true;
    }

    public static String orderBitsValue(TypeDefinition type, String bitsValue) {
        if (type instanceof BitsTypeDefinition) {
            List<BitsTypeDefinition.Bit> bits = ((BitsTypeDefinition) type).getBits();
            String[] split = StringUtils.split(bitsValue);
            if (split.length > 1) {
                StringBuilder builder = new StringBuilder();
                for (BitsTypeDefinition.Bit bit : bits) {
                    for (String splitBit : split) {
                        if (bit.getName().equals(splitBit)) {
                            builder.append(splitBit + " ");
                        }
                    }
                }
                builder.setLength(builder.length() - 1);
                return builder.toString();
            }
        }
        return bitsValue;
    }
}
