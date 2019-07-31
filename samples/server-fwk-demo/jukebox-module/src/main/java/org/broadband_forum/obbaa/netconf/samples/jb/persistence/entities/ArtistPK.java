package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import java.io.Serializable;
import java.util.Objects;

public class ArtistPK implements Serializable {
    public ArtistPK() {
    }
    private String parentId;
    private String name;;

    public ArtistPK(String parentId, String name) {
        this();
        this.parentId = parentId;
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArtistPK ArtistPK = (ArtistPK) o;
        return Objects.equals(parentId, ArtistPK.parentId) &&
                Objects.equals(name, ArtistPK.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, name);
    }

    @Override
    public String toString() {
        return "ArtistPK{" +
                "parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
