package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import java.io.Serializable;

/**
 * Created by keshava on 1/29/16.
 */
public class PlayListPK implements Serializable {
    
	private static final long serialVersionUID = -7256690373095733063L;
	String parentId;
    String name;

    @Override
    public boolean equals(Object o) {
        if (this == o){
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }

        PlayListPK that = (PlayListPK) o;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null){
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
