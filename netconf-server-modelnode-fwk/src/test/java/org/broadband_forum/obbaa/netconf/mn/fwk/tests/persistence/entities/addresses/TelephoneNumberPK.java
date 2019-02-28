package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses;

import java.io.Serializable;

/**
 * Created by keshava on 12/8/15.
 */
public class TelephoneNumberPK implements Serializable{
    
    private String type;
    private String parentId;
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parentId == null) ? 0 : parentId.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        TelephoneNumberPK other = (TelephoneNumberPK) obj;
        if (parentId == null) {
            if (other.parentId != null)
                return false;
        } else if (!parentId.equals(other.parentId))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
    
}
