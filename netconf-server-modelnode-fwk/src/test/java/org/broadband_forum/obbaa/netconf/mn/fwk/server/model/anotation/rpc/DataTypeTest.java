package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by vgotagi on 10/18/16.
 */
public class DataTypeTest {

    @Test
    public void testIsSingleValueType(){
        assertTrue(DataType.isSingleValueType(Number.class));
        assertFalse(DataType.isSingleValueType(DataType.class));
    }

    @Test
    public void testIsArrayOrCollection(){
        assertTrue(DataType.isArrayOrCollection(ArrayList.class));
        assertFalse(DataType.isArrayOrCollection(DataType.class));
    }

    @Test
    public void testIsInteger(){
        assertTrue(DataType.isInteger(Integer.class));
        assertTrue(DataType.isInteger(int.class));
        assertFalse(DataType.isInteger(DataType.class));
    }

    @Test
    public void testIsLong(){
        assertTrue(DataType.isLong(Long.class));
        assertTrue(DataType.isLong(long.class));
        assertFalse(DataType.isLong(Integer.class));
    }

    @Test
    public void testFloat(){
        assertTrue(DataType.isFloat(Float.class));
        assertTrue(DataType.isFloat(float.class));
        assertFalse(DataType.isFloat(Double.class));
    }

    @Test
    public void testDouble(){
        assertTrue(DataType.isDouble(Double.class));
        assertTrue(DataType.isDouble(double.class));
        assertFalse(DataType.isDouble(Integer.class));
    }

    @Test
    public void testIsByte(){
        assertTrue(DataType.isByte(Byte.class));
        assertTrue(DataType.isByte(byte.class));
        assertFalse(DataType.isByte(Integer.class));
    }

    @Test
    public void testIsBoolean(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIstring(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIsEnum(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIsBigInteger(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIsBigDecimal(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIshort(){
        assertTrue(DataType.isShort(Short.class));
        assertTrue(DataType.isShort(short.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testIsDate(){
        assertTrue(DataType.isDouble(Double.class));
        assertFalse(DataType.isShort(Integer.class));
    }

    @Test
    public void testICalendar(){
        assertTrue(DataType.isCalendar(Calendar.class));
        assertFalse(DataType.isCalendar(Integer.class));
    }

    @Test
    public void testIsCurrency(){
        assertTrue(DataType.isCurrency(Currency.class));
        assertFalse(DataType.isCurrency(Integer.class));
    }

    @Test
    public void testcreateInstanceFrom(){
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Boolean.class,"true").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Integer.class,"100").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Long.class,"10000").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Float.class,"100.10F").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Double.class,"100.10").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(BigInteger.class,"100000").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(BigDecimal.class,"1000000").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Byte.class,"1").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Short.class,"2").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Calendar.class,"1988-10-10").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Date.class,"1988-10-10").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Currency.class,"USD").getClass()));
            assertFalse(String.class.isAssignableFrom(DataType.createInstanceFrom(Mobile.class,"APPLE").getClass()));
    }
}

enum Mobile {
    Samsung(100),MOTOROLA(200),APPLE(3000);
    int price;
    Mobile(int p){
        price = p;
    }
    int showPrice(){
        return price;
    }
}
