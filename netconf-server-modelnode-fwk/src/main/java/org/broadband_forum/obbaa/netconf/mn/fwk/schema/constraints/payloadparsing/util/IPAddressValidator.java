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

import org.broadband_forum.obbaa.netconf.api.util.StringUtil;

import java.util.regex.Pattern;

public class IPAddressValidator {

    public static final String LEFT_BRACKET = "[";
    public static final String RIGHT_BRACKET = "]";

    private static final Pattern IP_V6_ADDRESS_PATTERN = Pattern.compile("((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|" +
            "(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))");

    public static boolean isValidIpV6(String ip) {
        if (StringUtil.isEmpty(ip)) {
            return false;
        }
        return IP_V6_ADDRESS_PATTERN.matcher(ip).matches();
    }

    public static String encapsulateIPV6(String ip) {
        if (isValidIpV6(ip)) {
            return LEFT_BRACKET + ip + RIGHT_BRACKET;
        }
        return ip;
    }
}
