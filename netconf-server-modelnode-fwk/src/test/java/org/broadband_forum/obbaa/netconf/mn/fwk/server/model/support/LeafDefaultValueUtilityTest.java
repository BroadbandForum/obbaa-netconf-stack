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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LogCallEntry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;



public class LeafDefaultValueUtilityTest {

    private LeafDefaultValueUtility m_leafDefaultValueUtility;
    private AdvancedLogger m_logger;
    private List<LogCallEntry> m_logCallBuffer;
    
    @Before
    public void setUp() throws Exception {
        m_leafDefaultValueUtility = new LeafDefaultValueUtility();
        m_logger = LeafDefaultValueUtility.getLoggerForTest();
        m_logCallBuffer = new ArrayList<>();
        m_logger.setLogCallBuffer(m_logCallBuffer);
    }

    @After
    public void tearDown() throws Exception {
        m_leafDefaultValueUtility = null;
        m_logger.removeLogCallBuffer();
    }
    
    @Test
    public void testGetDefaultValueWhenInt8Type() throws TransformerException {
        
        TypeDefinition type = mock(Int8TypeDefinition.class);
        
        //test octal value
        assertGetDefaultValue(type, "57", "071");

        //test single digit decimal value
        assertGetDefaultValue(type, "0", "0");
        
        //test decimal value
        assertGetDefaultValue(type, "127", "127");
        
        //test hex value
        assertGetDefaultValue(type, "104", "0x68");
        
        //test negative octal value
        assertGetDefaultValue(type, "-80", "-0120");
        
        //test negative decimal value
        assertGetDefaultValue(type, "-126", "-126");
        
        //test negative hex value
        assertGetDefaultValue(type, "-105", "-0X69");
    }


    @Test
    public void testGetDefaultValueWhenInt16Type() throws TransformerException {
        
        TypeDefinition type = mock(Int16TypeDefinition.class);
        
        //test octal value
        assertGetDefaultValue(type, "3648", "07100");
        
        //test single digit decimal value
        assertGetDefaultValue(type, "0", "0");

        //test decimal value
        assertGetDefaultValue(type, "12800", "12800");
        
        //test hex value
        assertGetDefaultValue(type, "26624", "0x6800");
        
        //test negative octal value
        assertGetDefaultValue(type, "-5120", "-012000");
        
        //test negative decimal value
        assertGetDefaultValue(type, "-12600", "-12600");
        
        //test negative hex value
        assertGetDefaultValue(type, "-26880", "-0X6900");
    }
    
    @Test
    public void testGetDefaultValueWhenInt32Type() throws TransformerException {
        
        TypeDefinition type = mock(Int32TypeDefinition.class);
        
        //test octal value
        assertGetDefaultValue(type, "233472", "0710000");
        
        //test single digit decimal value
        assertGetDefaultValue(type, "0", "0");

        //test decimal value
        assertGetDefaultValue(type, "1280000", "1280000");
        
        //test hex value
        assertGetDefaultValue(type, "6815744", "0x680000");
        
        //test negative octal value
        assertGetDefaultValue(type, "-327680", "-01200000");
        
        //test negative decimal value
        assertGetDefaultValue(type, "-1260000", "-1260000");
        
        //test negative hex value
        assertGetDefaultValue(type, "-6881280", "-0X690000");
    }
    
    @Test
    public void testGetDefaultValueWhenInt64Type() throws TransformerException {
        
        TypeDefinition type = mock(Int64TypeDefinition.class);
        
        //test octal value
        assertGetDefaultValue(type, "956301312", "07100000000");
        
        //test single digit decimal value
        assertGetDefaultValue(type, "0", "0");

        //test decimal value
        assertGetDefaultValue(type, "12800000000", "12800000000");
        
        //test hex value
        assertGetDefaultValue(type, "446676598784", "0x6800000000");
        
        //test negative octal value
        assertGetDefaultValue(type, "-1342177280", "-012000000000");
        
        //test negative decimal value
        assertGetDefaultValue(type, "-12600000000", "-12600000000");
        
        //test negative hex value
        assertGetDefaultValue(type, "-450971566080", "-0X6900000000");
    }
    
    @Test
    public void testGetDefaultValueWhenUint8Type() throws TransformerException {
        
        TypeDefinition type = mock(Uint8TypeDefinition.class);
        
        //test octal value
        assertGetDefaultValue(type, "72", "0110");
        
        //test single digit decimal value
        assertGetDefaultValue(type, "0", "0");

        //test decimal value
        assertGetDefaultValue(type, "256", "256");
        
        //test hex value
        assertGetDefaultValue(type, "138", "0x8a");
    }

    @Test
    public void testGetDefaultValueWhenUint16Type() throws TransformerException {
        
        TypeDefinition type = mock(Uint16TypeDefinition.class);
        
       //test octal value
       assertGetDefaultValue(type, "29696", "072000");
       
       //test single digit decimal value
       assertGetDefaultValue(type, "0", "0");

       //test decimal value
       assertGetDefaultValue(type, "25600", "25600");
       
       //test hex value
       assertGetDefaultValue(type, "34984", "0x88a8");
    }
    
    @Test
    public void testGetDefaultValueWhenUint32Type() throws TransformerException {
        
        TypeDefinition type = mock(Uint32TypeDefinition.class);
        
       //test octal value
       assertGetDefaultValue(type, "1900544", "07200000");
       
       //test single digit decimal value
       assertGetDefaultValue(type, "0", "0");

       //test decimal value
       assertGetDefaultValue(type, "256000000", "256000000");
       
       //test hex value
       assertGetDefaultValue(type, "2292711424", "0x88a80000");
    }
    
    @Test
    public void testGetDefaultValueWhenUint64Type() throws TransformerException {
        
        TypeDefinition type = mock(Uint64TypeDefinition.class);
        
       //test octal value
       assertGetDefaultValue(type, "498216206336", "07200000000000");
       
       //test single digit decimal value
       assertGetDefaultValue(type, "0", "0");

       //test decimal value
       assertGetDefaultValue(type, "2560000000000", "2560000000000");
       
       //test hex value
       assertGetDefaultValue(type, "150255135883264", "0x88a800000000");
    }

    @Test
    public void testGetDefaultValueEmpty() throws TransformerException {
        EmptyTypeDefinition type = mock(EmptyTypeDefinition.class);
        assertGetDefaultValue(type,"",null);
    }
    
    @Test
    public void testUnsupportedType() throws Exception{
        BitsTypeDefinition type = mock(BitsTypeDefinition.class);
    	assertGetDefaultValue(type, "LineProfile1", "LineProfile1");
    }
    
    @Test
    public void testUnionType() throws Exception {
        UnionTypeDefinition unionType = mock(UnionTypeDefinition.class);
        List<TypeDefinition<?>> memberTypes = new ArrayList<TypeDefinition<?>>();
        memberTypes.add(BaseTypes.uint32Type());
        
        EnumTypeDefinition enumType = mock(EnumTypeDefinition.class);
        List<EnumPair> enums = new ArrayList<>();
        EnumPair enumValue = mock(EnumPair.class);
        when(enumValue.getName()).thenReturn("unbounded");
        enums.add(enumValue);
        when(enumType.getValues()).thenReturn(enums);
        memberTypes.add(enumType);
        
        when(unionType.getTypes()).thenReturn(memberTypes);
        
        assertGetDefaultValue(unionType, "unbounded", "unbounded");
        // only debug messages should be present, no warnings
        for (LogCallEntry logCallEntry: m_logCallBuffer) {
            assertEquals("debug", logCallEntry.getMethod().getName());
        }
        
        assertGetDefaultValue(unionType, "0", "0");
        // only debug messages should be present, no warnings
        for (LogCallEntry logCallEntry: m_logCallBuffer) {
            assertEquals("debug", logCallEntry.getMethod().getName());
        }
    }

    @Test
    public void testGetDefaultValueWhenIdentityRef() throws TransformerException {
        TypeDefinition type = mock(IdentityrefTypeDefinition.class);
        assertGetDefaultValue(type, "a:b", "a:b");
    }

    @Test
    public void testGetDefaultValueWhenInstanceidentifier() throws TransformerException {
        TypeDefinition type = mock(InstanceIdentifierTypeDefinition.class);
        assertGetDefaultValue(type, "/ex:system/ex:services/ex:ssh", "/ex:system/ex:services/ex:ssh");
    }

        private void assertGetDefaultValue(TypeDefinition type, String expectedValue, String testValue) throws TransformerException {
        LeafSchemaNode leafSchemaNode =  mock(LeafSchemaNode.class);
        when(type.getDefaultValue()).thenReturn(testValue == null ? Optional.empty() : Optional.of(testValue));
        when(leafSchemaNode.getType()).thenReturn(type);
        String defaultValue = m_leafDefaultValueUtility.getDefaultValue(leafSchemaNode);        
        assertEquals(expectedValue, defaultValue);
    }
}
