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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.ParseKeyValuePredicateUtil.fetchKeyValuePairs;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.junit.Test;

public class ParseKeyValuePredicateUtilTest {

    @Test
    public void testVariousScenariosFor1KeyValue() {
        String keyAttribute = "[pf1:key1='pf2:value1']";
        List<Pair<String, String>> expected = new ArrayList<>();
        expected.add(new Pair<>("key1", "pf2:value1"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[key1='pf2:value1']";
        expected.add(new Pair<>("key1", "pf2:value1"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[pf1:key1='value1']";
        expected.add(new Pair<>("key1", "value1"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[key1='value1']";
        expected.add(new Pair<>("key1", "value1"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));
    }

    @Test
    public void testVariousScenariosFor2KeyValues() {
        String keyAttribute = "[pf1:key1='pf2:value1'][pf3:key2='pf4:value2']";
        List<Pair<String, String>> expected = new ArrayList<>();
        expected.add(new Pair<>("key1", "pf2:value1"));
        expected.add(new Pair<>("key2", "pf4:value2"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[key1='pf2:value1'][pf3:key2='value2']";
        expected.add(new Pair<>("key1", "pf2:value1"));
        expected.add(new Pair<>("key2", "value2"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[key1='value1'][key2='value2']";
        expected.add(new Pair<>("key1", "value1"));
        expected.add(new Pair<>("key2", "value2"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));
    }

    @Test
    public void testVariousScenariosFor3KeyValues() {
        String keyAttribute = "[pf1:key1='pf2:value1'][pf3:key2='pf4:value2'][pf5:key3='pf6:value3']";
        List<Pair<String, String>> expected = new ArrayList<>();
        expected.add(new Pair<>("key1", "pf2:value1"));
        expected.add(new Pair<>("key2", "pf4:value2"));
        expected.add(new Pair<>("key3", "pf6:value3"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[pf1:key1='value1'][key2='pf4:value2'][pf5:key3='value3']";
        expected.add(new Pair<>("key1", "value1"));
        expected.add(new Pair<>("key2", "pf4:value2"));
        expected.add(new Pair<>("key3", "value3"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[pf1:key1='value1'][key2='pf4:value2'][pf5:key3='value3']";
        expected.add(new Pair<>("key1", "value1"));
        expected.add(new Pair<>("key2", "pf4:value2"));
        expected.add(new Pair<>("key3", "value3"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));

        expected.clear();
        keyAttribute = "[key1='value1'][key2='value2'][key3='value3']";
        expected.add(new Pair<>("key1", "value1"));
        expected.add(new Pair<>("key2", "value2"));
        expected.add(new Pair<>("key3", "value3"));
        assertEquals(expected, fetchKeyValuePairs(keyAttribute));
    }
}
