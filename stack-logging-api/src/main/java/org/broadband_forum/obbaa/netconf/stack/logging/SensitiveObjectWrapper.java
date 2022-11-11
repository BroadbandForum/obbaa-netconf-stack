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

package org.broadband_forum.obbaa.netconf.stack.logging;

import java.util.Objects;

public class SensitiveObjectWrapper {
    protected final Object m_inner;

    public SensitiveObjectWrapper(Object inner) {
        m_inner = inner;
    }

    @Override
    public String toString() {
        if (m_inner != null) {
            return m_inner.toString();
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SensitiveObjectWrapper that = (SensitiveObjectWrapper) o;
        return Objects.equals(m_inner, that.m_inner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_inner);
    }
}
