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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;


public class LeafDefaultValueUtilityTest {

    private LeafDefaultValueUtility m_leafDefaultValueUtility;

    @Before
    public void setUp() throws Exception {
        m_leafDefaultValueUtility = new LeafDefaultValueUtility();
    }

    @After
    public void tearDown() throws Exception {
        m_leafDefaultValueUtility = null;
    }

    @Test
    public void testGetDefaultValueWhenInt8Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.int8Type(), "57", "071");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.int8Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.int8Type(), "127", "127");

        //test hex value
        assertGetDefaultValue(BaseTypes.int8Type(), "104", "0x68");

        //test negative octal value
        assertGetDefaultValue(BaseTypes.int8Type(), "-80", "-0120");

        //test negative decimal value
        assertGetDefaultValue(BaseTypes.int8Type(), "-126", "-126");

        //test negative hex value
        assertGetDefaultValue(BaseTypes.int8Type(), "-105", "-0X69");
    }


    @Test
    public void testGetDefaultValueWhenInt16Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.int16Type(), "3648", "07100");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.int16Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.int16Type(), "12800", "12800");

        //test hex value
        assertGetDefaultValue(BaseTypes.int16Type(), "26624", "0x6800");

        //test negative octal value
        assertGetDefaultValue(BaseTypes.int16Type(), "-5120", "-012000");

        //test negative decimal value
        assertGetDefaultValue(BaseTypes.int16Type(), "-12600", "-12600");

        //test negative hex value
        assertGetDefaultValue(BaseTypes.int16Type(), "-26880", "-0X6900");
    }

    @Test
    public void testGetDefaultValueWhenInt32Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.int32Type(), "233472", "0710000");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.int32Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.int32Type(), "1280000", "1280000");

        //test hex value
        assertGetDefaultValue(BaseTypes.int32Type(), "6815744", "0x680000");

        //test negative octal value
        assertGetDefaultValue(BaseTypes.int32Type(), "-327680", "-01200000");

        //test negative decimal value
        assertGetDefaultValue(BaseTypes.int32Type(), "-1260000", "-1260000");

        //test negative hex value
        assertGetDefaultValue(BaseTypes.int32Type(), "-6881280", "-0X690000");
    }

    @Test
    public void testGetDefaultValueWhenInt64Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.int64Type(), "956301312", "07100000000");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.int64Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.int64Type(), "12800000000", "12800000000");

        //test hex value
        assertGetDefaultValue(BaseTypes.int64Type(), "446676598784", "0x6800000000");

        //test negative octal value
        assertGetDefaultValue(BaseTypes.int64Type(), "-1342177280", "-012000000000");

        //test negative decimal value
        assertGetDefaultValue(BaseTypes.int64Type(), "-12600000000", "-12600000000");

        //test negative hex value
        assertGetDefaultValue(BaseTypes.int64Type(), "-450971566080", "-0X6900000000");
    }

    @Test
    public void testGetDefaultValueWhenUint8Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.uint8Type(), "72", "0110");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.uint8Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.uint8Type(), "256", "256");

        //test hex value
        assertGetDefaultValue(BaseTypes.uint8Type(), "138", "0x8a");
    }

    @Test
    public void testGetDefaultValueWhenUint16Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.uint16Type(), "29696", "072000");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.uint16Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.uint16Type(), "25600", "25600");

        //test hex value
        assertGetDefaultValue(BaseTypes.uint16Type(), "34984", "0x88a8");
    }

    @Test
    public void testGetDefaultValueWhenUint32Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.uint32Type(), "1900544", "07200000");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.uint32Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.uint32Type(), "256000000", "256000000");

        //test hex value
        assertGetDefaultValue(BaseTypes.uint32Type(), "2292711424", "0x88a80000");
    }

    @Test
    public void testGetDefaultValueWhenUint64Type() throws TransformerException {

        //test octal value
        assertGetDefaultValue(BaseTypes.uint64Type(), "498216206336", "07200000000000");

        //test single digit decimal value
        assertGetDefaultValue(BaseTypes.uint64Type(), "0", "0");

        //test decimal value
        assertGetDefaultValue(BaseTypes.uint64Type(), "2560000000000", "2560000000000");

        //test hex value
        assertGetDefaultValue(BaseTypes.uint64Type(), "150255135883264", "0x88a800000000");
    }

    @Test
    public void testGetDefaultValueEmpty() throws TransformerException {
        EmptyTypeDefinition type = mock(EmptyTypeDefinition.class);
        assertGetDefaultValue(type, "", null);
    }

    @Test
    public void testUnsupportedType() throws Exception {
        assertGetDefaultValue(BaseTypes.bitsTypeBuilder(SchemaPath.ROOT).build(), "LineProfile1", "LineProfile1");
    }

    private void assertGetDefaultValue(TypeDefinition type, String expectedValue, String testValue) throws
            TransformerException {
        LeafSchemaNode leafSchemaNode = mock(LeafSchemaNode.class);
        when(leafSchemaNode.getType()).thenReturn(type);
        when(leafSchemaNode.getDefault()).thenReturn(testValue);
        String defaultValue = m_leafDefaultValueUtility.getDefaultValue(leafSchemaNode);
        assertEquals(expectedValue, defaultValue);
    }
}
