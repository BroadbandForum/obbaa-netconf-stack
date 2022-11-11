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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class ValidationHint {
    public static final ValidationHint SKIP_IMPACT_ON_CREATE = new ValidationHint("SKIP_IMPACT_ON_CREATE");
    public static final ValidationHint SKIP_IMPACT_VALIDATION = new ValidationHint("SKIP_IMPACT_VALIDATION");
    public static final ValidationHint SKIP_VALIDATION = new ValidationHint("SKIP_VALIDATION");
    public static Map<String, ValidationHint> c_hints = new HashMap<>();
    static {
        c_hints.put(SKIP_IMPACT_ON_CREATE.name(), SKIP_IMPACT_ON_CREATE);
        c_hints.put(SKIP_IMPACT_VALIDATION.name(), SKIP_IMPACT_VALIDATION);
        c_hints.put(SKIP_VALIDATION.name(), SKIP_VALIDATION);
    }

    public static ValidationHint autoHint(ValidationHint hint) {
        return new ValidationHint(hint.name(), true);
    }

    public static ValidationHint valueOf(String hint) {
        return c_hints.get(hint);
    }

    private final boolean m_autoHint;
    private final String m_name;
    private ValidationHint(String name) {
        this(name, false);
    }

    private ValidationHint(String name, boolean autoHint) {
        m_name = name;
        m_autoHint = autoHint;
    }

    public boolean isAutoHint() {
        return m_autoHint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationHint that = (ValidationHint) o;
        return Objects.equals(m_name, that.m_name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name);
    }

    public String name() {
        return m_name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", ValidationHint.class.getSimpleName() + "[", "]")
                .add("m_autoHint=" + m_autoHint)
                .add("m_name='" + m_name + "'")
                .toString();
    }
}
