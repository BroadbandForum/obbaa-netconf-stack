package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model;

import java.io.Serializable;

public class OrderByUserListPk implements Serializable {

    private static final long serialVersionUID = -2712121918756650262L;
    private String someKey;
    private String parentId;
    
    public OrderByUserListPk() {}

    public String getSomeKey() {
        return someKey;
    }

    public String getParentId() {
        return parentId;
    }

    public void setSomeKey(String someKey) {
        this.someKey = someKey;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
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
        OrderByUserListPk other = (OrderByUserListPk) obj;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
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
        return "OrderByUserListPk [someKey=" + someKey + ", parentId=" + parentId + "]";
    }
    
}
