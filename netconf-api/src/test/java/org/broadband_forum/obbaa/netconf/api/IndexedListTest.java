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

package org.broadband_forum.obbaa.netconf.api;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class IndexedListTest {
    IndexedList<String, CharacterInfo> m_indexedList;
    private List<CharacterInfo> m_characters;
    private CharacterInfo m_heisenberg;
    private CharacterInfo m_pinkman;
    private CharacterInfo m_gus;

    @Before
    public void setUp() {
        m_indexedList = new IndexedList<>();
        m_characters = new ArrayList<>();
        m_heisenberg = new CharacterInfo("Heisenberg", "Bryan Cranston");
        m_pinkman = new CharacterInfo("Pinkman", "Aaron Paul");
        m_gus = new CharacterInfo("Gus", "Giancarlo Esposito");
        m_characters.add(m_heisenberg);
        m_characters.add(m_pinkman);
        m_characters.add(m_gus);
    }

    @Test
    public void testAddAddsAndIndexesObjects(){
        createCharactersInOrder(m_heisenberg, m_pinkman, m_gus);
        assertEquals(m_characters, m_indexedList.list());

        assertAllCharactersAreIndexed();

    }

    public void createCharactersInOrder(CharacterInfo first, CharacterInfo second, CharacterInfo third) {
        m_indexedList.add(first);
        m_indexedList.add(second);
        m_indexedList.add(third);
    }

    public void assertAllCharactersAreIndexed() {
        assertEquals(m_heisenberg, m_indexedList.map().get("Heisenberg"));
        assertEquals(m_pinkman, m_indexedList.map().get("Pinkman"));
        assertEquals(m_gus, m_indexedList.map().get("Gus"));
        assertEquals(3, m_indexedList.map().entrySet().size());
    }

    @Test
    public void testAddAddsMaintainsOrder(){
        createCharacters();

        Collections.reverse(m_characters);
        assertEquals(m_characters, m_indexedList.list());

        assertAllCharactersAreIndexed();
    }

    public void createCharacters() {
        createCharactersInOrder(m_gus, m_pinkman, m_heisenberg);
    }

    @Test
    public void testAddWithIndex(){
        m_indexedList.add(m_heisenberg);
        m_indexedList.add(0, m_pinkman);
        m_indexedList.add(m_gus);

        assertEquals(Arrays.asList(m_pinkman, m_heisenberg, m_gus), m_indexedList.list());

        assertAllCharactersAreIndexed();
    }

    @Test
    public void testRemoveWithKey(){
        createCharacters();
        m_indexedList.remove("Pinkman");
        assertEquals(Arrays.asList(m_gus, m_heisenberg), m_indexedList.list());

        assertEquals(m_heisenberg, m_indexedList.map().get("Heisenberg"));
        assertNull(m_indexedList.map().get("Pinkman"));
        assertEquals(m_gus, m_indexedList.map().get("Gus"));

        m_indexedList.remove("Pinkman");
        assertEquals(Arrays.asList(m_gus, m_heisenberg), m_indexedList.list());

        assertEquals(m_heisenberg, m_indexedList.map().get("Heisenberg"));
        assertNull(m_indexedList.map().get("Pinkman"));
        assertEquals(m_gus, m_indexedList.map().get("Gus"));
    }

    @Test
    public void testClear(){
        createCharacters();
        m_indexedList.clear();

        assertEquals(Collections.emptyList(), m_indexedList.list());
        assertEquals(Collections.emptyMap(), m_indexedList.map());
    }

    private class CharacterInfo implements IndexedList.IndexableListEntry<String> {
        private final String m_character;
        private final String m_actor;

        public CharacterInfo(String character, String actor) {
            m_character = character;
            m_actor = actor;
        }

        @Override
        public String getKey() {
            return m_character;
        }

        @Override
        public String toString() {
            return "CharacterInfo{" +
                    "m_character='" + m_character + '\'' +
                    ", m_actor='" + m_actor + '\'' +
                    '}';
        }
    }
}
