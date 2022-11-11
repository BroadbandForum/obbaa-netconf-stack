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

package org.broadband_forum.obbaa.netconf.api.utils.tree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NaturalOrderComparatorTest {

    @Test
    public void testStringSortUsingCustomComparator(){
        List<String> versions = Arrays.asList("11","1","2", "3", "10","9");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("1","2", "3", "9", "10", "11"), versions);

        versions = Arrays.asList("11","1","2", "3", "10","09");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("1","2", "3", "09", "10", "11"), versions);

        versions = Arrays.asList("11","1","2", "test3", "10","09");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("1","2", "09", "10", "11",  "test3"), versions);

        versions = Arrays.asList("test11","test1","test2", "test3", "test10","test09");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("test1","test2", "test3", "test09", "test10", "test11"), versions);

        versions = Arrays.asList("test11","test1","test2", "test3", "test10","test09", "test-9");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("test-9", "test1","test2", "test3", "test09", "test10", "test11"), versions);

        versions = Arrays.asList("1.1","1.2","2.1", "02.1", "10","09.01","09.10", "-9.01");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("-9.01", "1.1","1.2","2.1", "02.1", "09.01","09.10", "10"), versions);

        versions = Arrays.asList("test1.1","test1.2","test2.1", "test02.1", "test10","test09");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("test1.1","test1.2","test2.1", "test02.1","test09",  "test10"), versions);

        versions = Arrays.asList("22.03","21.09", "19B.12","19A.03","19A.06", "19.12", "19.03", "22.6", "21.03", "21.06", "22.90", "v1.0", "v11.0", "v9.0","22.06");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("19.03","19.12", "19A.03","19A.06","19B.12", "21.03", "21.06", "21.09", "22.03","22.06", "22.6", "22.90", "v1.0", "v9.0","v11.0"), versions);

        versions = Arrays.asList("20A.06", "19B.12","19A.03","19A.06", "22.6", "21.03", "21.04", "22.9", "22.06","20A.9","21.09");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("19A.03", "19A.06", "19B.12", "20A.06", "20A.9", "21.03", "21.04", "21.09", "22.06", "22.6", "22.9"), versions);
    }

    @Test
    public void testNaturalOrderComparator1() {
        List<String> versions = Arrays.asList("9", "10", "Wxyz", "Des", "abc", "a9", "b10", "xyz", "Des");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("9","10", "a9", "abc", "b10", "Des", "Des", "Wxyz", "xyz"), versions);
    }

    @Test
    public void testNaturalOrderComparator1WithCase() {
        List<String> versions = Arrays.asList("9", "10", "XYZ", "Des", "abc", "a9", "b10", "xyz", "Des");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("9","10", "a9", "abc", "b10", "Des", "Des", "XYZ", "xyz"), versions);
    }

    @Test
    public void testNaturalOrderComparator1WithCaseAndDigit() {
        String[] inputArray = {"9", "10", "XYZ123", "Des", "abc", "a9", "b10", "xyz", "Des"};
        String[] expectedArray = {"9","10", "a9", "abc", "b10", "Des", "Des", "xyz", "XYZ123"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testNaturalOrderComparator1WithCompareRight() {
        String[] inputArray = {"9", "10", "XYZ123", "Des", "abc", "a9", "b10", "XYZ1234", "Des"};
        String[] expectedArray = {"9","10", "a9", "abc", "b10", "Des", "Des", "XYZ123", "XYZ1234"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testNaturalOrderComparator1WithLeadingZero() {
        String[] inputArray = {"1000", "100", "XYZ123", "Des", "abc", "a9", "b10", "XYZ1234", "Des"};
        String[] expectedArray = {"100","1000", "a9", "abc", "b10", "Des", "Des", "XYZ123", "XYZ1234"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testNaturalOrderComparator1WithSpaceChar() {
        String[] inputArray = {"1000", "100", "XYZ123", "Des", "abc", " 0a1a", " 0a1A", "XYZ1234", "Des"};
        String[] expectedArray = {" 0a1A", " 0a1a", "100","1000", "abc", "Des", "Des", "XYZ123", "XYZ1234"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testNaturalOrderComparator1WithNull() {
        String[] inputArray = {"1000", "100", "Des", null, "abc", " 0a1a", " 0a1A", "XYZ123", null};
        String[] expectedArray = {null, null, " 0a1A", " 0a1a", "100","1000", "abc", "Des", "XYZ123"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testCompare1() {
        NaturalOrderComparator natural = new NaturalOrderComparator();
        assertEquals(0, natural.compare("abc1", "abc1"));
        assertEquals(-1, natural.compare("abc1", "abc2"));
        assertEquals(Integer.MIN_VALUE, natural.compare(null, "abc2"));
        assertEquals(Integer.MAX_VALUE, natural.compare("abc", null));
    }

    @Test
    public void testCompare2() {
        NaturalOrderComparator natural = new NaturalOrderComparator();
        assertEquals(0,natural.compare("  ", "        "));
        assertEquals(0,natural.compare("  1", "        1"));
        assertEquals(1,natural.compare("  11", "        2"));
        assertEquals(1,natural.compare("        211", "   11"));
    }

    @Test
    public void testCompareWithLeadingZeros() {
        String[] inputArray = {"009", "10", "08","090","0040"};
        String[] expectedArray = {"08", "009", "10", "0040", "090"};
        Arrays.sort(inputArray, new NaturalOrderComparator());
        assertArrayEquals(inputArray, expectedArray);
    }

    @Test
    public void testCompareWithDigit() {
        List<String> versions = Arrays.asList("20.006", "20.06", "20.0006", "20.050", "20.060", "20.00006", "20.005","20.0005","20.05", "20.004","20.00009", "20.055", "20.05500");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("20.00006", "20.00009", "20.0005", "20.0006", "20.004", "20.005", "20.006", "20.05", "20.050", "20.055", "20.05500", "20.06","20.060"), versions);

        versions = Arrays.asList("20.006", "20.03", "20.06", "20.6");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("20.006", "20.03", "20.06", "20.6"), versions);

    }

    @Test
    public void testCompareWithDigit1() {
        List<String> versions = Arrays.asList("20.06", "20.1");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("20.06","20.1"), versions);
    }

    @Test
    public void testCompareWithDigit2() {
        List<String> versions = Arrays.asList("10", "2.1");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("2.1","10"), versions);
    }

    @Test
    public void testCompareWithDigit3() {
        List<String> versions = Arrays.asList("09", "1.1", "9","5.2");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("1.1","5.2","09","9"), versions);
    }

    @Test
    public void testCompareWithDigit4() {
        List<String> versions = Arrays.asList( "9.41b","9.42a","09.41a", "9.41A","9.41a");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("9.41A", "09.41a", "9.41a", "9.41b", "9.42a"), versions);
    }

    @Test
    public void testCompareFileVersion() {
        List<String> versions = Arrays.asList("v5.55", "v1","v7.9","v2.4","V4.5", "V1.5","v5.70","v09.1","v12.6","V05.6");
        Collections.sort(versions, new NaturalOrderComparator());
        assertEquals(Arrays.asList("v1", "V1.5", "v2.4", "V4.5","v5.55", "V05.6","v5.70", "v7.9", "v09.1", "v12.6"), versions);
    }
}
