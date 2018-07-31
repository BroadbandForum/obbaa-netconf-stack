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

import java.math.BigInteger;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NaturalOrderComparator implements Comparator<String> {

    public static final Pattern DIGIT_BLOCK_PATTERN = Pattern.compile("^(\\d+)"); //$NON-NLS-1$
    public static final Pattern NON_DIGIT_BLOCK_PATTERN = Pattern.compile("^(\\D*)"); //$NON-NLS-1$

    public static final Pattern DIGIT_PATTERN = Pattern.compile("^\\d"); //$NON-NLS-1$
    public static final Pattern NON_DIGIT_PATTERN = Pattern.compile("^\\D"); //$NON-NLS-1$

    private int checkNullParams(String params1, String params2) {
        if (params1 == null && params2 == null) {
            return 0;
        }
        if (params1 == null && params2 != null) {
            return Integer.MIN_VALUE;
        }
        if (params1 != null && params2 == null) {
            return Integer.MAX_VALUE;
        }
        return -1;
    }

    @Override
    public int compare(String params1, String params2) {
        int cmpVal;
        // Need to check for null cases.
        int result = checkNullParams(params1, params2);

        if (result != -1) {
            return result;
        }

        while ((params1.length() != 0) && (params2.length() != 0)) {
            if (DIGIT_PATTERN.matcher(params1).find()) {
                if (NON_DIGIT_PATTERN.matcher(params2).find()) {
                    return params1.compareTo(params2);
                }
                Matcher m = DIGIT_BLOCK_PATTERN.matcher(params1);
                params1 = m.replaceFirst(""); //$NON-NLS-1$
                BigInteger s1field = new BigInteger(m.group(1));
                m = DIGIT_BLOCK_PATTERN.matcher(params2);
                params2 = m.replaceFirst(""); //$NON-NLS-1$
                BigInteger s2field = new BigInteger(m.group(1));
                cmpVal = s1field.compareTo(s2field);
            } else {
                if (DIGIT_PATTERN.matcher(params2).find()) {
                    return params1.compareTo(params2);
                }
                Matcher m = NON_DIGIT_BLOCK_PATTERN.matcher(params1);
                params1 = m.replaceFirst(""); //$NON-NLS-1$
                String s1field = m.group(1);
                m = NON_DIGIT_BLOCK_PATTERN.matcher(params2);
                params2 = m.replaceFirst(""); //$NON-NLS-1$
                String s2field = m.group(1);
                cmpVal = s1field.compareTo(s2field);
            }
            if (cmpVal != 0) {
                return cmpVal;
            }
        }
        if ((params1.length() == 0) && (params2.length() == 0)) {
            return 0;
        }
        if (params1.length() == 0) {
            return -1;
        }
        return 1;
    }

}
