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
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

@YangList(name = "someInnerList", namespace = VALIDATION_NS, revision = VALIDATION_REVISION)
@Entity
@IdClass(value = SomeInnerListPK.class)
public class SomeInnerList implements Serializable, Comparable<SomeInnerList>{

    private static final long serialVersionUID = 7661265557840669083L;

    @Id
    @YangListKey
    @Column
    private String someKey;
    
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;
    
    @YangSchemaPath
    @Column(length = 1000, nullable=true)
    private String schemaPath;
    
    @YangXmlSubtree
    @Column(length = 100000, nullable=true)
    private String xml;

    @YangChild
    @OneToMany(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<OrderByUserList> orderByUserList = new LinkedHashSet<OrderByUserList>();
    
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

    public String getSomeKey() {
        return someKey;
    }

    public void setSomeKey(String someKey) {
        this.someKey = someKey;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public Set<OrderByUserList> getOrderByUserList() {
        return orderByUserList;
    }

    public void setOrderByUserList(Set<OrderByUserList> orderByUserList) {
        this.orderByUserList = orderByUserList;
    }



    @Override
    public String toString() {
        return "SomeInnerList [someKey=" + someKey + ", parentId=" + parentId + ", schemaPath=" + schemaPath + ", xml=" + xml
                + ", orderByUserList=" + orderByUserList + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((orderByUserList == null) ? 0 : orderByUserList.hashCode());
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((schemaPath == null) ? 0 : schemaPath.hashCode());
        result = prime * result + ((someKey == null) ? 0 : someKey.hashCode());
        result = prime * result + ((xml == null) ? 0 : xml.hashCode());
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
        SomeInnerList other = (SomeInnerList) obj;
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
        if (someKey == null) {
            if (other.someKey != null)
                return false;
        } else if (!someKey.equals(other.someKey))
            return false;
        if (xml == null) {
            if (other.xml != null)
                return false;
        } else if (!xml.equals(other.xml))
            return false;
        return true;
    }

    @Override
    public int compareTo(SomeInnerList o) {
        return someKey.compareTo(o.getSomeKey());
    }

    
}
