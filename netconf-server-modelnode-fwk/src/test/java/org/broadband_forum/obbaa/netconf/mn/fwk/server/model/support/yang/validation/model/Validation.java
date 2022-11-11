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
import javax.persistence.OneToMany;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;


@YangContainer(name="validation", namespace = VALIDATION_NS , revision=VALIDATION_REVISION)
@Entity
public class Validation implements Serializable{
    private static final long serialVersionUID = -2093214494292603580L;

    @Id
    @YangParentId
    @Column(name=YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;
    
    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;
    
    @YangChild
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<SomeList> someList = new LinkedHashSet<SomeList>();

    @YangXmlSubtree
    @Column(length=100000)
    private String xmlSubTree;
    
    @YangChild
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderByUserList> orderByUserList = new LinkedHashSet<OrderByUserList>();
    
    public Validation(){}

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getXmlSubTree() {
        return xmlSubTree;
    }

    public void setXmlSubTree(String xmlSubTree) {
        this.xmlSubTree = xmlSubTree;
    }

   
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderByUserList == null) ? 0 : orderByUserList.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((someList == null) ? 0 : someList.hashCode());
        result = prime * result + ((xmlSubTree == null) ? 0 : xmlSubTree.hashCode());
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
        Validation other = (Validation) obj;
        if (orderByUserList == null) {
            if (other.orderByUserList != null)
                return false;
        } else if (!orderByUserList.equals(other.orderByUserList))
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
        if (someList == null) {
            if (other.someList != null)
                return false;
        } else if (!someList.equals(other.someList))
            return false;
        if (xmlSubTree == null) {
            if (other.xmlSubTree != null)
                return false;
        } else if (!xmlSubTree.equals(other.xmlSubTree))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Validation [parentId=" + parentId + ", schemaPath=" + schemaPath + ", someList=" + someList + ", xmlSubTree=" + xmlSubTree
                + ", orderByUserList=" + orderByUserList + "]";
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public Set<SomeList> getSomeList() {
        return someList;
    }

    public void setSomeList(Set<SomeList> someList) {
        this.someList = someList;
    }

    public Set<OrderByUserList> getOrderByUserList() {
        return orderByUserList;
    }

    public void setOrderByUserList(Set<OrderByUserList> orderByUserList) {
        this.orderByUserList = orderByUserList;
    }


    
}
