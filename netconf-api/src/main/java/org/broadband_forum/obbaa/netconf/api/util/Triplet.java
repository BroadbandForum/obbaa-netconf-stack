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

public class Triplet<T, U, V> {

    private final T m_first;
    private final U m_second;
    private final V m_third;

    private Triplet(T first, U second, V third) {
        this.m_first = first;
        this.m_second = second;
        this.m_third = third;
    }
    
    public static <T, U, V> Triplet<T, U, V> of(T first, U second, V third) {
        return new Triplet<>(first, second, third);
    }

    public T getFirst() {
        return m_first;
    }

    public U getSecond() {
        return m_second;
    }

    public V getThird() {
        return m_third;
    }

    @Override
    public String toString() {
        return "Triplet [m_first=" + m_first + ", m_second=" + m_second + ", m_third=" + m_third + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_first == null) ? 0 : m_first.hashCode());
        result = prime * result + ((m_second == null) ? 0 : m_second.hashCode());
        result = prime * result + ((m_third == null) ? 0 : m_third.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Triplet<?, ?, ?> other = (Triplet<?, ?, ?>) obj;
        if (m_first == null) {
            if (other.m_first != null)
                return false;
        } else if (!m_first.equals(other.m_first)) {
            return false;
        }
        if (m_second == null) {
            if (other.m_second != null)
                return false;
        } else if (!m_second.equals(other.m_second)) {
            return false;
        }
        if (m_third == null) {
            if (other.m_third != null) {
                return false;
            }
        } else if (!m_third.equals(other.m_third)) {
            return false;
        }
        return true;
    }
}
