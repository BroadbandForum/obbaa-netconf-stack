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

package org.broadband_forum.obbaa.netconf.persistence.jpa.query;

import java.util.EnumSet;

public enum QueryConditionOperator {
    EQUALS(false),
    NOT_EQUALS(true),
    LESS_THAN(false),
    GREATER_THAN(false),
    LESS_THAN_OR_EQUALS(false),
    GREATER_THAN_OR_EQUALS(false),
    IS_NULL(false),
    IS_NOT_NULL(true),
    LIKE(false),
    BEGIN_WITH(false),
    NOT_BEGIN_WITH(true),
    END_WITH(false),
    NOT_END_WITH(true),
    CONTAINS(false),
    NOT_CONTAINS(true),
    IN(false),
    NOT_IN(true);

    public static final EnumSet<QueryConditionOperator> UNARY_OPERATORS = EnumSet.of(IS_NULL, IS_NOT_NULL);

    private static final long serialVersionUID = 1L;

    private boolean m_isNegative;

    private QueryConditionOperator(boolean isNegative) {
        m_isNegative = isNegative;
    }

    public boolean isNegative() {
        return m_isNegative;
    }
}
