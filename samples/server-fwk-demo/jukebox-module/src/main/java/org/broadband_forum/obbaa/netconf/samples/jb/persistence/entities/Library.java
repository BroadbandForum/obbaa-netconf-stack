package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;

@YangContainer(name = "library", namespace = JBConstants.JB_NS, revision = JBConstants.JB_REVISION)
@Entity(name="library")
@Table(name="jb_library")
public class Library {
    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangChild
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Artist> artists = new HashSet<>();

    @YangChild
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Singer> singers = new HashSet<>();

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Library library = (Library) o;
        return Objects.equals(parentId, library.parentId) &&
                Objects.equals(schemaPath, library.schemaPath);
    }

    public Set<Artist> getArtists() {
        return artists;
    }

    public void setArtists(Set<Artist> artists) {
        this.artists = artists;
    }

    public Set<Singer> getSingers() {
        return singers;
    }

    public void setSingers(Set<Singer> singers) {
        this.singers = singers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, schemaPath);
    }
}
