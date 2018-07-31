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
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@YangList(name = "someList", namespace = VALIDATION_NS, revision = VALIDATION_REVISION)
@Entity
@IdClass(value = SomeListPK.class)
public class SomeList implements Serializable {

    private static final long serialVersionUID = -8468993424088459287L;

    @Id
    @YangListKey
    @Column(unique = true)
    private String someKey;

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    @YangChild
    private Set<SomeInnerList> someInnerList = new LinkedHashSet<SomeInnerList>();

    public Set<SomeInnerList> getSomeInnerList() {
        return someInnerList;
    }

    public void setSomeInnerList(Set<SomeInnerList> someInnerList) {
        this.someInnerList = someInnerList;
    }

    public String getSomeKey() {
        return someKey;
    }

    public void setSomeKey(String someKey) {
        this.someKey = someKey;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
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
        SomeList other = (SomeList) obj;
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

    @Override
    public String toString() {
        return "SomeList [someKey=" + someKey + ", parentId=" + parentId + ", schemaPath=" + schemaPath + "]";
    }


}
