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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IndexedList<KeyType,  ValueType extends IndexedList.IndexableListEntry<KeyType>> {
    private List<ValueType> m_list = new ArrayList<>();
    private Map<KeyType, ValueType> m_index = new HashMap<>();

    public void add(ValueType value) {
       add(m_list.size(), value);
    }

    public List<ValueType> list() {
        return m_list;
    }

    public Map<KeyType, ValueType> map() {
        return m_index;
    }

    public void add(int index, ValueType value) {
        m_list.add(index, value);
        m_index.put(value.getKey(), value);
    }

    public ValueType remove(KeyType key) {
        ValueType value = m_index.remove(key);
        if(value != null){
            m_list.remove(value);
        }
        return value;
    }

    public void clear() {
        m_list.clear();
        m_index.clear();
    }

    public boolean isEmpty() {
        return m_list.isEmpty();
    }

    public int size() {
        return m_list.size();
    }

    public ValueType get(int index) {
        return m_list.get(index);
    }

    public interface IndexableListEntry<KeyType> {
        KeyType getKey();
    }
}
