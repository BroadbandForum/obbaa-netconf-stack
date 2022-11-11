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

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {

    @Override
    public int compare(String string1, String string2) {
        if (string1 == string2) // NOSONAR
            return 0;

        if (string1 == null)
            return Integer.MIN_VALUE;

        if (string2 == null)
            return Integer.MAX_VALUE;

        int comp = compareIgnoreCase(string1, string2);
        if (comp == 0) {
            comp = compareCase(string1, string2);
        }
        return comp;
    }

    /**
     * @param string1
     * @param string2
     */
    private int compareIgnoreCase(String string1, String string2) {
        int ia = 0, ib = 0;
        int nza = 0, nzb = 0;
        char ca, cb;
        char nextca, nextcb;
        int result, resultZero;
        boolean hasDotChar = false;

        while (true) {
            // only count the number of zeroes leading the last number compared
            nza = nzb = 0;

            ca = toLowerCharAt(string1, ia);
            cb = toLowerCharAt(string2, ib);

            //next character
            nextca = toLowerCharAt(string1, ia +1);
            nextcb = toLowerCharAt(string2, ib +1);

            // ignore leading 'space' characters OR zeros (only if next character also be digit)
            while (isSpaceORZeroChar(ca, nextca)) {
                if (ca == '0') {
                    nza++;
                } else {
                    nza = 0;
                }
                ca = toLowerCharAt(string1, ++ia);
                nextca = toLowerCharAt(string1, ia +1);
            }

            while (isSpaceORZeroChar(cb, nextcb)) {
                if (cb == '0') {
                    nzb++;
                } else {
                    nzb = 0;
                }
                cb = toLowerCharAt(string2, ++ib);
                nextcb = toLowerCharAt(string2, ib +1);
            }
            if(isDotChar(ca, cb)){
                hasDotChar = true; // If both characters are dot
            }

            // process run of digits
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                if ((result = compareRight(string1.substring(ia), string2.substring(ib), hasDotChar)) != 0) {
                    // compare the count of zeros after dot character
                    if(hasDotChar && (resultZero = compareZeroCount(nza, nzb))!= 0) {
                        return resultZero;
                    }
                    return result;
                }
            }

            if (ca == 0 && cb == 0) {
                // The strings compare the same. Perhaps the caller
                // will want to call strcmp to break the tie.
                return nza - nzb;
            }

            if (ca < cb) {
                return -1;
            } else if (ca > cb) {
                return +1;
            }

            ++ia;
            ++ib;
        }
    }

    /**
     * @param string1
     * @param string2
     */
    private int compareCase(String string1, String string2) {
        int ia = 0, ib = 0;
        int nza = 0, nzb = 0;
        char ca, cb;
        char nextca, nextcb;
        int result, resultZero;
        boolean hasDotChar = false;

        while (true) {
            // only count the number of zeroes leading the last number compared
            nza = nzb = 0;

            ca = charAt(string1, ia);
            cb = charAt(string2, ib);

            // next characters
            nextca = charAt(string1, ia +1);
            nextcb = charAt(string2, ib +1);

            // ignore leading 'space' characters, ignore zeros only if next character also be digit
            while (isSpaceORZeroChar(ca, nextca)) {
                if (ca == '0') {
                    nza++;
                } else {
                    nza = 0;
                }
                ca = charAt(string1, ++ia);
                nextca = charAt(string1, ia +1);
            }

            while (isSpaceORZeroChar(cb, nextcb)) {
                if (cb == '0') {
                    nzb++;
                } else {
                    // Only count consecutive zeroes
                    nzb = 0;
                }
                cb = charAt(string2, ++ib);
                nextcb = charAt(string2, ib +1);
            }
            if(isDotChar(ca, cb)){
                hasDotChar = true;
            }

            // process run of digits
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                if ((result = compareRight(string1.substring(ia), string2.substring(ib), hasDotChar)) != 0) {
                    return result;
                }
            }

            if (ca == 0 && cb == 0) {
                // The strings compare the same. Perhaps the caller
                // will want to call strcmp to break the tie.
                return nza - nzb;
            }

            if (ca < cb) {
                return -1;
            } else if (ca > cb) {
                return +1;
            }

            // process the number zeros after dot character
            if(hasDotChar && (resultZero = compareZeroCount(nza, nzb))!= 0) {
                return resultZero;
            }

            ++ia;
            ++ib;
        }
    }

    private int compareRight(String a, String b, boolean isDot) {
        int bias = 0;
        int ia = 0;
        int ib = 0;

        // The longest run of digits wins. That aside, the greatest
        // value wins, but we can't know that it will until we've scanned
        // both numbers to know that they have the same magnitude, so we
        // remember it in BIAS.
        for (;; ia++, ib++) {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);

            if (!Character.isDigit(ca) && !Character.isDigit(cb)) {
                return bias;
            } else if (!Character.isDigit(ca)) {
                return -1;
            } else if (!Character.isDigit(cb)) {
                return +1;
            } else if (ca < cb) {
                if (bias == 0) {
                    bias = -1;
                }
            } else if (ca > cb) {
                if (bias == 0)
                    bias = +1;
            } else if (ca == 0 || cb == 0) {
                return bias;
            }

            // processing for compare the digits after dot character
            if(isDot && ca != cb){
                return bias;
            }
        }
    }

    /**
     * Provides character at specified index
     * @param string
     * @param index
     * @return the character at specified index
     */
    private static char charAt(String string, int index) {
        if (index >= string.length()) {
            return 0;
        } else {
            return string.charAt(index);
        }
    }

    /**
     * Provides lower case character equivalent at specified index
     * @param string
     * @param index
     * @return the lower case character equivalent at specified index
     */
    private static char toLowerCharAt(String string, int index) {
        if (index >= string.length()) {
            return 0;
        } else {
            //return string.charAt(index);
            return Character.toLowerCase(string.charAt(index));
        }
    }

    // return the values based on compare the count of zeros after dot character.
    private int compareZeroCount(int countA, int countB){
        if(countA < countB){
            return 1;
        } else if (countA > countB){
            return -1;
        }
        return 0;
    }

    private static boolean isSpaceORZeroChar(char c, char nc){
        return Character.isSpaceChar(c) || ( c == '0' && Character.isDigit(nc));
    }

    private static boolean isDotChar(char ca, char cb) {
        return ca == '.' && cb == '.';
    }
}
