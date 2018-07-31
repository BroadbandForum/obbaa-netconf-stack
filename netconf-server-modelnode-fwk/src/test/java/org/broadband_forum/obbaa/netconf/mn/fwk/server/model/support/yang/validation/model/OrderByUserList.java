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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.ValidationConstants.VALIDATION_REVISION;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@YangList(name = "orderByUserList", namespace = VALIDATION_NS, revision = VALIDATION_REVISION)
@Entity
@Table
@IdClass(OrderByUserListPk.class)
public class OrderByUserList implements Serializable {

    private static final long serialVersionUID = 2668297649461350849L;

    @Id
    @YangListKey
    @Column(unique = true)
    private String someKey;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @Column
    @YangOrderByUser
    private Integer insertOrder;

    public OrderByUserList() {
    }

    public String getSomeKey() {
        return someKey;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public String getParentId() {
        return parentId;
    }

    public Integer getInsertOrder() {
        return insertOrder;
    }

    public void setSomeKey(String someKey) {
        this.someKey = someKey;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setInsertOrder(Integer insertOrder) {
        this.insertOrder = insertOrder;
    }

    @Override
    public String toString() {
        return "OrderByUserList [someKey=" + someKey + ", schemaPath=" + schemaPath + ", parentId=" + parentId + ", " +
                "insertOrder="
                + insertOrder + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((insertOrder == null) ? 0 : insertOrder.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((someKey == null) ? 0 : someKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OrderByUserList other = (OrderByUserList) obj;
        if (insertOrder == null) {
            if (other.insertOrder != null)
                return false;
        } else if (!insertOrder.equals(other.insertOrder))
            return false;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (schemaPath == null) {
            if (other.schemaPath != null)
                return false;
        } else if (!schemaPath.equals(other.schemaPath))
            return false;
        if (someKey == null) {
            if (other.someKey != null)
                return false;
        } else if (!someKey.equals(other.someKey))
            return false;
        return true;
    }


}
