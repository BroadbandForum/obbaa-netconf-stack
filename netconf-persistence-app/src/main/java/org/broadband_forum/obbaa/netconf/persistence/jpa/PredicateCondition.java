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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public enum PredicateCondition {

    EQUAL,
    GREATER_THAN,
    GREATER_THAN_EQUAL,
    LESS_THAN,
    LESS_THAN_EQUAL,
    BETWEEN,
    LIKE;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addCondition(CriteriaBuilder cb, CriteriaQuery cq, Root<?> rootEntry, String fieldName, Double
            value1, Double value2) {
        switch (this) {
            case EQUAL:
                cq.where(cb.equal(rootEntry.<Double>get(fieldName), value1));
                break;
            case GREATER_THAN_EQUAL:
                cq.where(cb.greaterThanOrEqualTo(rootEntry.<Double>get(fieldName), value1));
                break;
            case GREATER_THAN:
                cq.where(cb.greaterThan(rootEntry.<Double>get(fieldName), value1));
                break;
            case LESS_THAN_EQUAL:
                cq.where(cb.lessThanOrEqualTo(rootEntry.<Double>get(fieldName), value1));
                break;
            case LESS_THAN:
                cq.where(cb.lessThan(rootEntry.<Double>get(fieldName), value1));
                break;
            case BETWEEN:
                cq.where(cb.between(rootEntry.<Double>get(fieldName), value1, value2));
                break;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void addLike(CriteriaBuilder cb, CriteriaQuery cq, Root<?> rootEntry, String fieldName, String value1) {
        switch (this) {
            case LIKE:
                cq.where(cb.like(rootEntry.<String>get(fieldName), "%" + value1 + "%"));
                break;
            default:
                throw new IllegalArgumentException("Expected to be called only for LIKE predicates");
        }
    }
}
