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

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;

@MappedSuperclass
public abstract class OrderedByUser extends YangTypeWithParentId implements Comparable<OrderedByUser> {
    @Column
    @YangOrderByUser
    protected Integer insertOrder = 0;

    public Integer getInsertOrder() {
        return insertOrder;
    }

    public void setInsertOrder(Integer insertOrder) {
        this.insertOrder = insertOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof OrderedByUser)) return false;

        OrderedByUser that = (OrderedByUser) o;

        return new EqualsBuilder()
                .appendSuper(super.equals(o))
                .append(insertOrder, that.insertOrder)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(insertOrder)
                .toHashCode();
    }

    @Override
    public int compareTo(OrderedByUser other) {
        return insertOrder.compareTo(other.insertOrder);
    }
}
