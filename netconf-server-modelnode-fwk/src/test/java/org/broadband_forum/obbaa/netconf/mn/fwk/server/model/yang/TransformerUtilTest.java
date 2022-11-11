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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.TransformerUtil;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

public class TransformerUtilTest {
    
    //For Integer type
    @Test
    public void testIntegerConvert() throws TransformerException{
        TypeDefinition type = mock(Uint16TypeDefinition.class);
        assertEquals(250, TransformerUtil.convert(type, "00250"));
    }

    //For String type
    @Test
    public void testStringConvert() throws TransformerException{
        TypeDefinition type = mock(StringTypeDefinition.class);
        assertEquals("StrValue", TransformerUtil.convert(type, "StrValue"));
    }

    //For Double type
    @Test
    public void testDoubleConvert() throws TransformerException{
        TypeDefinition type = mock(DecimalTypeDefinition.class);
        assertEquals("-0.55555", TransformerUtil.convert(type, "-0.55555"));
    }

    //For Long type
    @Test
    public void testLongConvert() throws TransformerException{
        TypeDefinition type = mock(Int64TypeDefinition.class);
        long expectedLong = -258986786;
        assertEquals(expectedLong, TransformerUtil.convert(type, "-00258986786"));
    }

    //For Empty type
    @Test
    public void testEmptyConvert() throws TransformerException{
        TypeDefinition type = mock(EmptyTypeDefinition.class);
        assertEquals("", TransformerUtil.convert(type, ""));
    }

    //For Enum Type
    @Test
    public void testEnumConvert() throws TransformerException{
        EnumTypeDefinition enumType = mock(EnumTypeDefinition.class);
        List<EnumTypeDefinition.EnumPair> enums = new ArrayList<>();
        EnumTypeDefinition.EnumPair enumValue1 = mock(EnumTypeDefinition.EnumPair.class);
        when(enumValue1.getName()).thenReturn("enum1");
        EnumTypeDefinition.EnumPair enumValue2 = mock(EnumTypeDefinition.EnumPair.class);
        when(enumValue2.getName()).thenReturn("enum2");
        enums.add(enumValue1);
        enums.add(enumValue2);
        when(enumType.getValues()).thenReturn(enums);

        assertEquals("enum1", TransformerUtil.convert(enumType, "enum1"));
        assertEquals("enum2", TransformerUtil.convert(enumType, "enum2"));
    }

    //For Enum Type(Invalid enum)
    @Test
    public void testInvalidEnumConvert(){
        EnumTypeDefinition enumType = mock(EnumTypeDefinition.class);
        List<EnumTypeDefinition.EnumPair> enums = new ArrayList<>();
        EnumTypeDefinition.EnumPair enumValue = mock(EnumTypeDefinition.EnumPair.class);
        when(enumValue.getName()).thenReturn("IANA");
        enums.add(enumValue);
        when(enumType.getValues()).thenReturn(enums);
        try {
            TransformerUtil.convert(enumType, "TEST");
            fail("Transformer Exception should have been thrown");
        } catch(TransformerException e){
            assertEquals("Invalid enum value",e.getMessage());
        }
    }

    //For Union type
    @Test
    public void testUnionConvert() throws TransformerException{
        UnionTypeDefinition unionType = mock(UnionTypeDefinition.class);
        List<TypeDefinition<?>> memberTypes = new ArrayList<>();

        EnumTypeDefinition enumType = mock(EnumTypeDefinition.class);
        List<EnumTypeDefinition.EnumPair> enums = new ArrayList<>();
        EnumTypeDefinition.EnumPair enumValue = mock(EnumTypeDefinition.EnumPair.class);
        when(enumValue.getName()).thenReturn("IANA");
        enums.add(enumValue);
        when(enumType.getValues()).thenReturn(enums);
        memberTypes.add(enumType);

        Uint16TypeDefinition intType = mock(Uint16TypeDefinition.class);
        memberTypes.add(intType);

        IdentityrefTypeDefinition identityrefType = mock(IdentityrefTypeDefinition.class);
        memberTypes.add(identityrefType);

        InstanceIdentifierTypeDefinition insatnceIdentifierType = mock(InstanceIdentifierTypeDefinition.class);
        memberTypes.add(insatnceIdentifierType);

        when(unionType.getTypes()).thenReturn(memberTypes);

        assertEquals("IANA", TransformerUtil.convert(unionType, "IANA"));
        assertEquals(0, TransformerUtil.convert(unionType, "0000"));
        assertEquals("mc:aes", TransformerUtil.convert(unionType, "mc:aes"));
        assertEquals("/ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']", TransformerUtil.convert(unionType, "/ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']"));
    }

    //For Nested Union type
    @Test
    public void testNestedUnionConvert() throws TransformerException{
        UnionTypeDefinition unionType = mock(UnionTypeDefinition.class);
        List<TypeDefinition<?>> memberTypes = new ArrayList<>();

        UnionTypeDefinition nestedUnionType = mock(UnionTypeDefinition.class);
        List<TypeDefinition<?>> nestedUnionMemberTypes = new ArrayList<>();

        Uint16TypeDefinition intType = mock(Uint16TypeDefinition.class);
        nestedUnionMemberTypes.add(intType);

        TypeDefinition stringType = mock(StringTypeDefinition.class);
        nestedUnionMemberTypes.add(stringType);

        when(nestedUnionType.getTypes()).thenReturn(nestedUnionMemberTypes);

        memberTypes.add(nestedUnionType);

        IdentityrefTypeDefinition identityrefType = mock(IdentityrefTypeDefinition.class);
        memberTypes.add(identityrefType);

        when(unionType.getTypes()).thenReturn(memberTypes);

        assertEquals("IANA", TransformerUtil.convert(unionType, "IANA"));
        assertEquals(0, TransformerUtil.convert(unionType, "0000"));
        assertEquals("mc:aes", TransformerUtil.convert(unionType, "mc:aes"));
        assertEquals("stringValue", TransformerUtil.convert(unionType, "stringValue"));
    }

    //For IdentityRef type
    @Test
    public void testIdentityRefConvert() throws Exception{
        TypeDefinition identityRefType = mock(IdentityrefTypeDefinition.class);
        assertEquals("mc:aes", TransformerUtil.convert(identityRefType, "mc:aes"));
    }

    //For InstanceIdentifier type
    @Test
    public void testInstanceIdentifierConvert() throws Exception{
        TypeDefinition instanceIdentifierType = mock(InstanceIdentifierTypeDefinition.class);
        assertEquals("/ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']", TransformerUtil.convert(instanceIdentifierType, "/ex:system/ex:server[ex:ip='192.0.2.1'][ex:port='80']"));
    }

    //For Exception case
    @Test(expected = TransformerException.class)
    public void testConvertThrowsException() throws TransformerException{
        Uint64TypeDefinition type = mock(Uint64TypeDefinition.class);
        TransformerUtil.convert(type,"stringValue");
    }
}
