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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import static org.broadband_forum.obbaa.netconf.api.util.DocumentUtils.COLON;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class ParseKeyValuePredicateUtil {

    public static List<Pair<String, String>> fetchKeyValuePairs(String keyAttribute) {
        String regex = "]\\["; // split keys
        keyAttribute = keyAttribute.substring(1,keyAttribute.length() - 1);
        String[] keyValues = keyAttribute.split(regex);
        List<Pair<String, String>> keyValuePairs = new ArrayList<>();
        for (String keyValue : keyValues) {
            String key;
            String value;
            key = keyValue.substring(0, keyValue.indexOf("="));
            if (key.contains(COLON)) {// if contains prefix, remove the prefix
                key = key.substring(key.indexOf(":") + 1).trim();
            }
            value = keyValue.substring(keyValue.indexOf("=") + 1).trim();
            value = value.substring(value.indexOf("'") + 1, value.lastIndexOf("'"));
            keyValuePairs.add(new Pair<>(key, value));
        }
        return keyValuePairs;
    }
}
