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

package org.broadband_forum.obbaa.netconf.api.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A Map implementation to support waiting behavior
 *
 * @param <K> Key
 * @param <V> Value
 * @author keshava
 */
@SuppressWarnings("unchecked")
public class BlockingMap<K, V> implements Map<K, V> {
    private static final String NOT_SUPPORTED = "Not supported";
    private final Map<K, BlockingQueue<V>> m_map = new ConcurrentHashMap<K, BlockingQueue<V>>();

    private synchronized BlockingQueue<V> ensureQueueExists(K key) {
        if (m_map.containsKey(key)) {
            return m_map.get(key);
        } else {
            BlockingQueue<V> queue = new ArrayBlockingQueue<V>(1);
            m_map.put(key, queue);
            return queue;
        }
    }

    public V put(K key, V value) {
        BlockingQueue<V> queue = ensureQueueExists(key);
        if (queue.offer(value)) {
            return value;
        }
        return null;
    }

    public V get(K key, long timeout, TimeUnit timeUnit) {
        BlockingQueue<V> queue = ensureQueueExists(key);
        try {
            V value = queue.poll(timeout, timeUnit);
            return value;
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    public V remove(Object key) {
        return (V) m_map.remove(key);
    }

    @Override
    public int size() {
        return m_map.size();
    }

    @Override
    public boolean isEmpty() {
        return m_map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return m_map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return m_map.containsValue(value);
    }

    /*
     * get and not wait
     */
    @Override
    public V get(Object key) {
        BlockingQueue<V> blockingQueue = m_map.get(key);
        if (blockingQueue != null) {
            return blockingQueue.poll();
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new RuntimeException(NOT_SUPPORTED);

    }

    @Override
    public void clear() {
        throw new RuntimeException(NOT_SUPPORTED);

    }

    @Override
    public Set<K> keySet() {
        throw new RuntimeException(NOT_SUPPORTED);
    }

    @Override
    public Collection<V> values() {
        throw new RuntimeException(NOT_SUPPORTED);
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        throw new RuntimeException(NOT_SUPPORTED);
    }
}