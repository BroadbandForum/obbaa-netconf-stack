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

public class Pair<F, S> {

    private F m_first;
    private S m_second;

    public Pair(F first, S second) {
        this.m_first = first;
        this.m_second = second;
    }

    public F getFirst() {
        return this.m_first;
    }

    public Pair<F, S> setFirst(F first) {
        this.m_first = first;
        return this;
    }

    public S getSecond() {
        return this.m_second;
    }

    public Pair<F, S> setSecond(S second) {
        this.m_second = second;
        return this;
    }

    @Override
    public String toString() {
        return "Pair [first=" + this.m_first + ", second=" + this.m_second + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.m_first == null) ? 0 : this.m_first.hashCode());
        result = prime * result + ((this.m_second == null) ? 0 : this.m_second.hashCode());
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
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (this.m_first == null) {
            if (other.m_first != null) {
                return false;
            }
        } else if (!this.m_first.equals(other.m_first)) {
            return false;
        }
        if (this.m_second == null) {
            if (other.m_second != null) {
                return false;
            }
        } else if (!this.m_second.equals(other.m_second)) {
            return false;
        }
        return true;
    }

}
