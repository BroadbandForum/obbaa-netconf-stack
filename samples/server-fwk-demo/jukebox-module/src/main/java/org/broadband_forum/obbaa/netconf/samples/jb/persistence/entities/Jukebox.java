package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import static org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants.JB_REVISION;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@YangContainer(name = "jukebox", namespace = JB_NS, revision = JB_REVISION)
@Entity(name="jukebox")
@Table(name="jb_jukebox")
public class Jukebox {
    @Id
    @YangParentId
    @Column(name=YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Library library;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private Player player;

    @YangChild
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlayList> playLists = new HashSet<>();

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

    public Library getLibrary() {
        return library;
    }

    public void setLibrary(Library library) {
        this.library = library;
    }

    public Set<PlayList> getPlayLists() {
        return playLists;
    }

    public void setPlayLists(Set<PlayList> playLists) {
        this.playLists = playLists;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Jukebox jukebox = (Jukebox) o;
        return Objects.equals(parentId, jukebox.parentId) &&
                Objects.equals(schemaPath, jukebox.schemaPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, schemaPath);
    }
}
