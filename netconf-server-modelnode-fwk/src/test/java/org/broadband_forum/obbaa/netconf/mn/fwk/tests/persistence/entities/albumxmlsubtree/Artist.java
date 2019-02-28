package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.ArtistPK;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "albumsubtree_artist")
@Table(name = "albumsubtree_artist")
@IdClass(ArtistPK.class)
@YangList(name="artist", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class Artist {
	public static final String NAME = "name";

	@Id
	@Column(name=NAME)
	@YangListKey(name="name", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@YangChild
	private Set<Album> albums = new HashSet<>();

	@Id
	@YangParentId
	String parentId;

	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;
	
	@Column
    @YangOrderByUser
	private Integer insertOrder;

	public String getSchemaPath() {
		return schemaPath;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
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


	public Set<Album> getAlbums() {
		return albums;
	}

	public void setAlbums(Set<Album> albums) {
		this.albums = albums;
	}

	public void addAlbums(Album album) {
		albums.add(album);
	}

	public int getInsertOrder() {
		return insertOrder;
	}
	
	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}
	
}
