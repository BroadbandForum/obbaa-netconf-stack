package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import java.io.Serializable;

public class SingerPK implements Serializable {

	private static final long serialVersionUID = -1590041170827389396L;
	String name;
	String parentId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SingerPK that = (SingerPK) o;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SingerPK{" +
                "singer='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}
