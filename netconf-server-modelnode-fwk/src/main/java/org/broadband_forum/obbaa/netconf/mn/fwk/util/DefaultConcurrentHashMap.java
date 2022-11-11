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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * A default concurrent map implementation that must be used across ANV code where there will be multi-threaded access.
 * 
 * The consumer of this instance, must simply call get(key) to retrieve the value and not do a if (contains()) check, 
 * unless it is for some other trivial reason.
 * 
 * K - Key Type
 * V - Return Value Type
 * 
 * Usage Examples:
 * 1) 
 * DefaultConcurrentHashMap<String, String> map1 = new DefaultConcurrentHashMap<String, String>("Hello");
 * String test = map1.get("a");
 * 
 * The above usage indicates 
 * a) A concurrent map of <String, String> is required
 * b) For every key that has no value, the default value of "Hello" would be returned. 
 * 
 * 2)
 * DefaultConcurrentHashMap<String, Map<?, ?>> map1 = new DefaultConcurrentHashMap<>(null);
 * Map test = map1.get("test");
 * 
 * The above usage indicates 
 * a) a concurrent map of <String,Map> is required 
 * b) There is no default value and null has to be returned when key not available.
 * 
 * 3)
 * Map<String, HashMap<String, String>> map1 = new DefaultConcurrentHashMap<>(new HashMap<String, String>(), false);
 * Map test = map1.get("a");
 * 
 * The above usage indicates
 * 1) a concurrent map of <String, HashMap> is required
 * 2) a HashMap<String, String> is created for each key that is not available by default
 * 3) All such value HashMap<String,String> are not synchronized maps or concurrent hashMap. 
 * 
 * 4)
 * Map<String, HashSet<String>> map1 = new DefaultConcurrentHashMap<>(new HashSet<String>(), true);
 * Set test = map1.get("a");
 * 
 * The above usage indicates
 * 1) a concurrent map of <String, HashSet> is required
 * 2) a HashSet<String> is created for each key that is not available by default
 * 3) All such value HashSet<String> are synchronized sets. 
 * 
 */
@SuppressWarnings("unchecked")
public class DefaultConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = -1582950539309428051L;

    protected V m_defaultValue;
    protected boolean m_createSynchronizedCollection;

    public DefaultConcurrentHashMap(V defaultValue) {
        this(defaultValue, false);
    }
    
    /**
     * For every key that is not available in the map, a default new Map/List/Set will be put in the map
     * based on the input instance type. 
     * 
     * If createSynchronizedCollection is true, the same value will be threadSafe.
     * 
     * If createSynchronizedCollection is false and the input instance type is not a collection, then the given 
     * instance is returned back
     * 
     * @param classInstance
     * @param createSynchronizedCollection
     */
    public DefaultConcurrentHashMap(V classInstance, boolean createSynchronizedCollection) {
        m_defaultValue = (V) classInstance;
        m_createSynchronizedCollection = createSynchronizedCollection;
    }

    @Override
    public V get(Object k) {

        if (k == null) {
            return null;
        }

        V value = super.get(k);
        return (value != null) ? value : getDefaultValue((K) k);
    }

    synchronized private V getDefaultValue(K k) {
        try {
            // check again in case a competing thread was held up at the method
            V returnValue = super.get(k);
            if (returnValue == null) {
                if (m_defaultValue != null) {
                    if (m_createSynchronizedCollection) {
                        if (m_defaultValue instanceof Map) {
                            returnValue = (V) Collections.synchronizedMap((Map<?, ?>) m_defaultValue.getClass().newInstance());
                        } else if (m_defaultValue instanceof Set) {
                            returnValue = (V) Collections.synchronizedSet((Set<?>) m_defaultValue.getClass().newInstance());
                        } else if (m_defaultValue instanceof List) {
                            returnValue = (V) Collections.synchronizedList((List<?>) m_defaultValue.getClass().newInstance());
                        } else if (m_defaultValue instanceof Collection) {
                            returnValue = (V) Collections.synchronizedCollection((Collection<?>) m_defaultValue.getClass().newInstance());
                        }
                    } else {
                        if (m_defaultValue instanceof Map) {
                            returnValue = (V) (Map<?, ?>) m_defaultValue.getClass().newInstance();
                        } else if (m_defaultValue instanceof Set) {
                            returnValue = (V) (Set<?>) m_defaultValue.getClass().newInstance();
                        } else if (m_defaultValue instanceof List) {
                            returnValue = (V) (List<?>) m_defaultValue.getClass().newInstance();
                        } else if (m_defaultValue instanceof Collection) {
                            returnValue = (V) (Collection<?>) m_defaultValue.getClass().newInstance();
                        } else {
                            returnValue = m_defaultValue;
                        }
                    }
                }
                if (returnValue != null) {
                    putIfAbsent(k, returnValue);
                    returnValue = super.get(k);
                }
            }
            return returnValue;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
